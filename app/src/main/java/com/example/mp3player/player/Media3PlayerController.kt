package com.example.mp3player.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mp3player.data.model.Song
import com.example.mp3player.data.repository.QueueRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import android.content.Intent
import com.example.mp3player.player.PlayerNotificationService
import com.example.mp3player.data.repository.PlayStatsRepository
import kotlinx.coroutines.Job
/** Gestisce la riproduzione delle canzoni. */
class Media3PlayerController(
    context: Context,
    private val queueRepository: QueueRepository,
    private val exoPlayer: ExoPlayer,
    private val playStatsRepository: PlayStatsRepository
    ) : PlayerController {

    private val appContext = context.applicationContext
    // Componenti principali del lettore e coroutine scope
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var statsRegistrationJob: Job? = null

    override fun pause() {
        exoPlayer.pause()
    }

    // Gestione dello stato di riproduzione osservabile
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    init {
        // Callback per intercettare i cambiamenti di ExoPlayer
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) onTrackEnded()
            }
        })
        startPositionUpdates()
    }

    // =========================================================================
    // COMANDI DI RIPRODUZIONE (Interfaccia PlayerController)
    // =========================================================================

    override fun play(song: Song, queue: List<Song>) {
        val effectiveQueue = if (_state.value.isShuffleEnabled) shuffled(queue, song) else queue
        val index = effectiveQueue.indexOf(song).takeIf { it >= 0 } ?: 0
        _state.update {
            it.copy(queue = effectiveQueue, currentIndex = index, currentSong = song, currentPositionMs = 0L)
        }
        loadAndPlay(song)
    }

    companion object {
        private const val SHUFFLE_HISTORY_SIZE = 5
    }

    // Cronologia degli ultimi brani ascoltati, usata dallo shuffle intelligente
    private val recentHistory = ArrayDeque<Long>()

    private fun registerPlayed(songId: Long) {
        recentHistory.remove(songId)          // se già presente, lo sposta in fondo
        recentHistory.addLast(songId)
        while (recentHistory.size > SHUFFLE_HISTORY_SIZE) {
            recentHistory.removeFirst()
        }
    }

    override fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _state.update { it.copy(currentPositionMs = positionMs) }
    }

    override fun setShuffleEnabled(enabled: Boolean, scope: List<Song>) {
        _state.update { st ->
            val newQueue = if (enabled) shuffled(scope, st.currentSong) else scope
            val newIndex = st.currentSong?.let { newQueue.indexOf(it).coerceAtLeast(0) } ?: st.currentIndex
            st.copy(isShuffleEnabled = enabled, queue = newQueue, currentIndex = newIndex)
        }
    }

    override fun setRepeatEnabled(enabled: Boolean) {
        _state.update { it.copy(isRepeatEnabled = enabled) }
    }

    override fun skipNext() {
        val st = _state.value
        if (st.isRepeatEnabled) {
            st.currentSong?.let { loadAndPlay(it) }
            return
        }
        val nextFromQueue = queueRepository.popNext()
        if (nextFromQueue != null) {
            playNextFromQueue(nextFromQueue)
            return
        }
        if (st.queue.isEmpty()) return

        if (st.isShuffleEnabled && st.currentIndex >= st.queue.lastIndex) {
            val freshQueue = shuffled(st.queue, null)
            val nextSong = freshQueue.first()
            _state.update {
                it.copy(queue = freshQueue, currentIndex = 0, currentSong = nextSong, currentPositionMs = 0L)
            }
            loadAndPlay(nextSong)
            return
        }

        val nextIndex = (st.currentIndex + 1) % st.queue.size
        val nextSong = st.queue[nextIndex]
        _state.update { it.copy(currentIndex = nextIndex, currentSong = nextSong, currentPositionMs = 0L) }
        loadAndPlay(nextSong)
    }

    override fun skipPrevious() {
        val st = _state.value
        if (st.isRepeatEnabled) {
            st.currentSong?.let { loadAndPlay(it) }
            return
        }
        if (exoPlayer.currentPosition > 3_000L) {
            exoPlayer.seekTo(0)
            return
        }
        if (st.queue.isEmpty()) return
        val prevIndex = if (st.currentIndex > 0) st.currentIndex - 1 else st.queue.lastIndex
        val prevSong = st.queue[prevIndex]
        _state.update { it.copy(currentIndex = prevIndex, currentSong = prevSong, currentPositionMs = 0L) }
        loadAndPlay(prevSong)
    }

    override fun release() {
        exoPlayer.release()
    }

    // =========================================================================
    // METODI PRIVATI DI SUPPORTO (Logica Interna)
    // =========================================================================

    private fun onTrackEnded() {
        skipNext()
    }

    private fun playNextFromQueue(song: Song) {
        val st = _state.value
        val idxInList = st.queue.indexOf(song)
        _state.update {
            it.copy(
                currentSong = song,
                currentIndex = if (idxInList >= 0) idxInList else it.currentIndex,
                currentPositionMs = 0L
            )
        }
        loadAndPlay(song)
    }

    private var currentSongStartTime: Long = 0L

    private fun loadAndPlay(song: Song) {
        val uri = Uri.fromFile(File(song.path))
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
        exoPlayer.play()
        ensureServiceStarted()
        currentSongStartTime = System.currentTimeMillis()
        registerPlayed(song.id)
    }

    private fun ensureServiceStarted() {
        val intent = Intent(appContext, PlayerNotificationService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            appContext.startForegroundService(intent)
        } else {
            appContext.startService(intent)
        }
    }
    // Loop periodico per aggiornare la barra di avanzamento (ogni 250ms)
    private fun startPositionUpdates() {
        scope.launch {
            var statsRegisteredForSongId: Long? = null

            while (true) {
                if (exoPlayer.isPlaying) {
                    _state.update { it.copy(currentPositionMs = exoPlayer.currentPosition) }

                    val song = _state.value.currentSong
                    if (song != null
                        && statsRegisteredForSongId != song.id
                        && System.currentTimeMillis() - currentSongStartTime >= 30_000L
                    ) {
                        statsRegisteredForSongId = song.id
                        playStatsRepository.registerPlay(song)
                    }
                }
                delay(250L)
            }
        }
    }

    // Mescola la lista mantenendo il brano corrente come primo elemento
// Mescola la lista evitando, quando ci sono abbastanza brani, che uno
// riappaia prima che ne siano passati almeno SHUFFLE_HISTORY_SIZE diversi.
    private fun shuffled(list: List<Song>, anchor: Song?): List<Song> {
        val pool = list.filter { it.id != anchor?.id }
        if (pool.isEmpty()) return listOfNotNull(anchor)

        val avoidIds = recentHistory.toSet() + setOfNotNull(anchor?.id)
        val windowSize = minOf(SHUFFLE_HISTORY_SIZE, pool.size)

        val remainingPool = pool.shuffled().toMutableList()
        val ordered = mutableListOf<Song>()

        // Riempiamo le prime posizioni solo con brani NON ascoltati di recente
        val safeFirst = remainingPool.filter { it.id !in avoidIds }.take(windowSize)
        ordered.addAll(safeFirst)
        remainingPool.removeAll(safeFirst)
        ordered.addAll(remainingPool)

        return if (anchor != null) listOf(anchor) + ordered else ordered
    }
}
