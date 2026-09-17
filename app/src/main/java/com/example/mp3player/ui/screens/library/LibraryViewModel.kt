package com.example.mp3player.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mp3player.data.model.Song
import com.example.mp3player.player.Media3PlayerController
import com.example.mp3player.player.PlaybackState
import kotlinx.coroutines.flow.StateFlow
import com.example.mp3player.data.repository.QueueRepository
import com.example.mp3player.data.repository.FavoritesRepository
import com.example.mp3player.data.repository.HiddenSongsRepository
import com.example.mp3player.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.mp3player.data.repository.PlayStatsRepository


class LibraryViewModel(
    private val playerController: Media3PlayerController,
    private val queueRepository: QueueRepository,
    private val favoritesRepository: FavoritesRepository,
    private val preferencesRepository: PreferencesRepository,
    private val hiddenSongsRepository: HiddenSongsRepository,
    private val playStatsRepository: PlayStatsRepository
) : ViewModel() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + viewModelScope.coroutineContext)

    val playbackState: StateFlow<PlaybackState> = playerController.state
    val favorites: StateFlow<List<Song>>        = favoritesRepository.favorites
    val hiddenIds: StateFlow<Set<Long>>         = hiddenSongsRepository.hiddenIds  // ← NUOVO

    init {
        // Salva posizione ogni 5 secondi mentre riproduce
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(5_000L)
                val st = playerController.state.value
                val song = st.currentSong ?: continue
                preferencesRepository.saveCurrentSong(song.id, st.currentPositionMs)
            }
        }
    }


    fun getPlayCount(song: Song): Int? =
        playStatsRepository.playStats.value
            .find { it.song.id == song.id }
            ?.playCount
            ?.takeIf { it > 0 }
    fun onSongClick(song: Song, queue: List<Song>) {
        // I brani nascosti possono comunque essere selezionati manualmente,
        // ma non devono restare nel pool per lo shuffle successivo.
        val effectiveQueue = queue.filter { !hiddenSongsRepository.isHidden(it) || it.id == song.id }
        playerController.play(song, effectiveQueue)
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.saveCurrentSong(song.id, 0L)
        }
    }

    fun onPlayPause()    = playerController.togglePlayPause()
    fun onSkipNext()     = playerController.skipNext()
    fun onSkipPrevious() = playerController.skipPrevious()
    fun onSeek(positionMs: Long) {
        playerController.seekTo(positionMs)
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.savePosition(positionMs)
        }
    }

    /**
     * Lo shuffle usa solo i brani NON nascosti come pool casuale.
     * [visibleSongs] è la lista già filtrata che arriva dalla UI.
     */
    fun onShuffleToggle(visibleSongs: List<Song>) {
        val next = !playerController.state.value.isShuffleEnabled
        val shufflePool = visibleSongs.filter { !hiddenSongsRepository.isHidden(it) }
        playerController.setShuffleEnabled(next, shufflePool)
        if (next) {                                        // ← nuovo: disattiva repeat
            playerController.setRepeatEnabled(false)
            viewModelScope.launch(Dispatchers.IO) { preferencesRepository.saveRepeat(false) }
            if (playerController.state.value.currentSong == null && shufflePool.isNotEmpty()) {
                playerController.play(shufflePool.random(), shufflePool)
            }
        }
        viewModelScope.launch(Dispatchers.IO) { preferencesRepository.saveShuffle(next) }
    }

    fun onRepeatToggle() {
        val next = !playerController.state.value.isRepeatEnabled
        playerController.setRepeatEnabled(next)
        if (next) {                                        // ← nuovo: disattiva shuffle
            playerController.setShuffleEnabled(false, playerController.state.value.queue)
            viewModelScope.launch(Dispatchers.IO) { preferencesRepository.saveShuffle(false) }
        }
        viewModelScope.launch(Dispatchers.IO) { preferencesRepository.saveRepeat(next) }
    }

    fun onSwipeNext()     = playerController.skipNext()
    fun onSwipePrevious() = playerController.skipPrevious()

    fun onSeekForward10() {
        val pos = (playerController.state.value.currentPositionMs + 10_000L)
            .coerceAtMost(playerController.state.value.currentSong?.duration ?: Long.MAX_VALUE)
        playerController.seekTo(pos)
    }

    fun onSeekBack10() {
        val pos = (playerController.state.value.currentPositionMs - 10_000L).coerceAtLeast(0L)
        playerController.seekTo(pos)
    }

    fun seekRelative(millis: Long) {
        val currentState = playerController.state.value
        val current = currentState.currentPositionMs
        val duration = currentState.currentSong?.duration ?: Long.MAX_VALUE
        val target = (current + millis).coerceIn(0L, duration)
        playerController.seekTo(target)
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }

    fun isFavorite(song: Song)  = favoritesRepository.isFavorite(song)
    fun isHidden(song: Song)    = hiddenSongsRepository.isHidden(song)

    fun onFavoriteToggle(song: Song) {
        // Non aggiungere ai preferiti un brano nascosto
        if (hiddenSongsRepository.isHidden(song)) return
        if (favoritesRepository.isFavorite(song)) favoritesRepository.remove(song)
        else favoritesRepository.add(song)
    }

    fun onAddToQueue(song: Song) {
        // Non aggiungere in coda un brano nascosto
        if (hiddenSongsRepository.isHidden(song)) return
        queueRepository.add(song)
    }

    /**
     * Nasconde o mostra un brano.
     * Se il brano era nei preferiti e viene nascosto, viene rimosso automaticamente.
     * Se era in coda, viene rimosso dalla coda.
     */
    fun onHideSongToggle(song: Song) {
        if (hiddenSongsRepository.isHidden(song)) {
            // Ri-mostra il brano
            hiddenSongsRepository.unhide(song)
        } else {
            // Nasconde il brano
            hiddenSongsRepository.hide(song)
            // Rimuove automaticamente dai preferiti
            if (favoritesRepository.isFavorite(song)) {
                favoritesRepository.remove(song)
            }
            // Rimuove dalla coda: cerca l'indice e rimuove
            val queueList = queueRepository.queue.value
            val idx = queueList.indexOfFirst { it.id == song.id }
            if (idx >= 0) {
                queueRepository.removeAt(idx)
            }
        }
    }
}