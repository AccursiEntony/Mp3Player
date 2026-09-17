package com.example.mp3player.di

import android.content.Context
import android.content.Intent
import com.example.mp3player.data.local.database.AppDatabase
import com.example.mp3player.data.local.datastore.UserPreferencesDataStore
import com.example.mp3player.data.repository.*
import com.example.mp3player.player.Media3PlayerController
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSessionService
import com.example.mp3player.player.PlayerNotificationService

/**
 * Contenitore delle dipendenze dell'applicazione (Dependency Injection manuale).
 * Gestisce il ciclo di vita e la creazione delle istanze centralizzate (Singleton-like).
 */
class AppContainer(context: Context) {

    // =========================================================================
    // 1. SORGENTI DATI LOCALI (Data Sources)
    // =========================================================================

    private val db = AppDatabase.getInstance(context)
    val preferencesDataStore = UserPreferencesDataStore(context)

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            true
        )
        .build()

    // =========================================================================
    // 2. REPOSITORY
    // =========================================================================

    val preferencesRepository: PreferencesRepository =
        PreferencesRepositoryImpl(preferencesDataStore, context)

    val queueRepository: QueueRepository =
        QueueRepositoryImpl(db.queueDao())

    val favoritesRepository: FavoritesRepository =
        FavoritesRepositoryImpl(db.favoriteDao())

    val musicRepository: MusicRepository =
        MusicRepositoryImpl(context)

    // ── NUOVO: repository brani nascosti ──────────────────────────────────────
    val hiddenSongsRepository: HiddenSongsRepository =
        HiddenSongsRepositoryImpl(context)

    // ── NUOVO: repository statistiche di ascolto ────────────────────────────
    val playStatsRepository: PlayStatsRepository =
        PlayStatsRepositoryImpl(db.playCountDao())

    // =========================================================================
    // 3. CONTROLLER DI SISTEMA
    // =========================================================================

    val playerController: Media3PlayerController =
        Media3PlayerController(context, queueRepository, exoPlayer, playStatsRepository)

    init {
        context.startService(Intent(context, PlayerNotificationService::class.java))
    }


}