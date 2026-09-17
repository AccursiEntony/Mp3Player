package com.example.mp3player.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.mp3player.MainActivity
import com.example.mp3player.Mp3PlayerApp
import com.example.mp3player.R
import com.example.mp3player.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.mp3player.widget.WidgetArtCache

class PlayerNotificationService : Service() {

    companion object {
        const val CHANNEL_ID      = "mp3player_channel"
        const val NOTIFICATION_ID = 1

        const val ACTION_PLAY_PAUSE = "com.example.mp3player.PLAY_PAUSE"
        const val ACTION_NEXT       = "com.example.mp3player.NEXT"
        const val ACTION_PREVIOUS   = "com.example.mp3player.PREVIOUS"
        const val EXTRA_FORCE_UPDATE = "force_update"

        const val ACTION_SHUFFLE = "com.example.mp3player.widget.SHUFFLE"
        const val ACTION_REPEAT  = "com.example.mp3player.widget.REPEAT"
    }

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var stateJob: Job? = null

    private lateinit var playerController: Media3PlayerController

    private var lastAlbumArtUri: String? = null
    private var cachedBitmap: Bitmap?    = null

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    override fun onCreate() {
        super.onCreate()

        val container = (application as Mp3PlayerApp).container
        playerController = container.playerController

        createNotificationChannel()
        initMediaSession()
        startForegroundCompat()
        observePlaybackState()

        // Forza aggiornamento immediato con lo stato attuale
        serviceScope.launch {
            val currentState = playerController.state.value
            if (currentState.currentSong != null) {
                val bitmap = loadAlbumArt(currentState.currentSong?.albumArtUri)
                val notification = buildNotification(currentState.currentSong, currentState.isPlaying, bitmap)
                notificationManager.notify(NOTIFICATION_ID, notification)
                sendWidgetUpdate(currentState, bitmap)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> playerController.togglePlayPause()
            ACTION_NEXT       -> playerController.skipNext()
            ACTION_PREVIOUS   -> playerController.skipPrevious()

            ACTION_SHUFFLE -> {
                val current = playerController.state.value
                playerController.setShuffleEnabled(!current.isShuffleEnabled, current.queue)
            }

            ACTION_REPEAT -> {
                val current = playerController.state.value
                playerController.setRepeatEnabled(!current.isRepeatEnabled)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stateJob?.cancel()
        mediaSession.release()
        super.onDestroy()
    }

    // =========================================================================
    // SETUP
    // =========================================================================

    private fun createNotificationChannel() {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Riproduzione musicale",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description          = "Controlli del lettore musicale"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initMediaSession() {
        val sessionActivityIntent = PendingIntent.getActivity(
            this, 1,   // FIX: requestCode 1 per non collidere con quello di buildNotification (0)
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSessionCompat(this, "Mp3PlayerSession").apply {
            setSessionActivity(sessionActivityIntent)
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay()              = playerController.togglePlayPause()
                override fun onPause()             = playerController.togglePlayPause()
                override fun onSkipToNext()        = playerController.skipNext()
                override fun onSkipToPrevious()    = playerController.skipPrevious()
                override fun onSeekTo(pos: Long)   = playerController.seekTo(pos)
            })
            isActive = true
        }
    }

    /**
     * FIX principale per la notifica non funzionante su Android 14+.
     *
     * Android 14 (API 34) richiede che startForeground dichiari il
     * foregroundServiceType corrispondente a quello nel Manifest.
     * ServiceCompat.startForeground gestisce questa differenza tra versioni
     * in modo sicuro senza richiedere un if/else manuale.
     *
     * Nel Manifest devi avere:
     *   <service android:name=".player.PlayerNotificationService"
     *       android:foregroundServiceType="mediaPlayback" />
     */
    private fun startForegroundCompat() {
        val notification = buildNotification(null, false, null)
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            else
                0
        )
    }

    // =========================================================================
    // OSSERVAZIONE DELLO STATO
    // =========================================================================

    private fun observePlaybackState() {
        stateJob = serviceScope.launch {
            var lastSongId: Long?   = null
            var lastIsPlaying: Boolean? = null

            // FIX: collectLatest invece di collect per evitare che un'emissione
            // lenta (caricamento copertina) blocchi le emissioni successive.
            playerController.state.collectLatest { state ->
                val songChanged    = state.currentSong?.id != lastSongId
                val playingChanged = state.isPlaying != lastIsPlaying

                if (!songChanged && !playingChanged) return@collectLatest

                lastSongId    = state.currentSong?.id
                lastIsPlaying = state.isPlaying

                updateMediaSession(state.currentSong, state.isPlaying, state.currentPositionMs)

                val bitmap       = loadAlbumArt(state.currentSong?.albumArtUri)
                val notification = buildNotification(state.currentSong, state.isPlaying, bitmap)
                notificationManager.notify(NOTIFICATION_ID, notification)

                sendWidgetUpdate(state, bitmap)
            }
        }
    }

    // =========================================================================
    // MEDIA SESSION (lock screen / Bluetooth / cuffie)
    // =========================================================================

    private fun updateMediaSession(song: Song?, isPlaying: Boolean, positionMs: Long) {
        val metadata = MediaMetadataCompat.Builder().apply {
            putString(MediaMetadataCompat.METADATA_KEY_TITLE,    song?.title  ?: "")
            putString(MediaMetadataCompat.METADATA_KEY_ARTIST,   song?.artist ?: "")
            putString(MediaMetadataCompat.METADATA_KEY_ALBUM,    song?.album  ?: "")
            putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song?.duration ?: 0L)
        }.build()
        mediaSession.setMetadata(metadata)

        val state = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING
                else           PlaybackStateCompat.STATE_PAUSED,
                positionMs,
                1f
            )
            .build()
        mediaSession.setPlaybackState(state)
    }

    // =========================================================================
    // COSTRUZIONE NOTIFICA CUSTOM
    // =========================================================================

    private fun buildNotification(
        song: Song?,
        isPlaying: Boolean,
        albumArt: Bitmap?
    ): Notification {

        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevAction = NotificationCompat.Action(
            R.drawable.outline_skip_previous_24,
            "Precedente",
            buildActionIntent(ACTION_PREVIOUS)
        )
        val playPauseAction = NotificationCompat.Action(
            if (isPlaying) R.drawable.outline_pause_24 else R.drawable.outline_play_arrow_24,
            if (isPlaying) "Pausa" else "Play",
            buildActionIntent(ACTION_PLAY_PAUSE)
        )
        val nextAction = NotificationCompat.Action(
            R.drawable.outline_skip_next_24,
            "Successivo",
            buildActionIntent(ACTION_NEXT)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.outline_music_note_24)
            .setContentTitle(song?.title ?: "Nessun brano")
            .setContentText(song?.artist ?: "")
            .setLargeIcon(albumArt)
            .setContentIntent(openAppIntent)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    /** Crea un PendingIntent che invia un'action a questo stesso Service. */
    private fun buildActionIntent(action: String): PendingIntent {
        val intent = Intent(this, PlayerNotificationService::class.java).apply {
            this.action = action
        }
        val requestCode = when (action) {
            ACTION_PLAY_PAUSE -> 10
            ACTION_NEXT       -> 11
            ACTION_PREVIOUS   -> 12
            else              -> 99
        }
        return PendingIntent.getService(
            this, requestCode, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    // =========================================================================
    // CARICAMENTO COPERTINA (con cache)
    // =========================================================================

    private suspend fun loadAlbumArt(uri: String?): Bitmap? {
        if (uri == null) {
            lastAlbumArtUri = null
            cachedBitmap    = null
            return null
        }
        if (uri == lastAlbumArtUri) return cachedBitmap

        return try {
            val loader  = ImageLoader(applicationContext)
            val request = ImageRequest.Builder(applicationContext)
                .data(uri)
                .allowHardware(false)
                .size(256, 256)
                .build()
            val result = loader.execute(request)
            val bitmap = (result as? SuccessResult)
                ?.drawable
                ?.let { (it as? BitmapDrawable)?.bitmap }
            lastAlbumArtUri = uri
            cachedBitmap    = bitmap
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun sendWidgetUpdate(
        state: com.example.mp3player.player.PlaybackState,
        bitmap: Bitmap?
    ) {
        val durationMs = state.currentSong?.duration ?: 0L
        val progress = if (durationMs > 0L)
            ((state.currentPositionMs.toFloat() / durationMs) * 1000).toInt()
        else 0

        WidgetArtCache.bitmap = bitmap

        val intent = Intent(this, com.example.mp3player.widget.PlayerWidget::class.java).apply {
            action = com.example.mp3player.widget.PlayerWidget.ACTION_UPDATE_WIDGET
            putExtra(com.example.mp3player.widget.PlayerWidget.EXTRA_TITLE,        state.currentSong?.title  ?: "Nessun brano")
            putExtra(com.example.mp3player.widget.PlayerWidget.EXTRA_ARTIST,       state.currentSong?.artist ?: "")
            putExtra(com.example.mp3player.widget.PlayerWidget.EXTRA_IS_PLAYING,   state.isPlaying)
            putExtra(com.example.mp3player.widget.PlayerWidget.EXTRA_PROGRESS,     progress)
            putExtra(com.example.mp3player.widget.PlayerWidget.EXTRA_PROGRESS_MAX, 1000)
            putExtra(com.example.mp3player.widget.PlayerWidget.EXTRA_SHUFFLE,      state.isShuffleEnabled)
            putExtra(com.example.mp3player.widget.PlayerWidget.EXTRA_REPEAT,       state.isRepeatEnabled)
        }
        sendBroadcast(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        playerController.pause()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }
}