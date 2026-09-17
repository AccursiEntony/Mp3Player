package com.example.mp3player.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.mp3player.MainActivity
import com.example.mp3player.R
import com.example.mp3player.player.PlayerNotificationService
import android.app.PendingIntent
import android.graphics.Color
import android.os.Build
import com.example.mp3player.widget.WidgetArtCache

class PlayerWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Viene chiamato all'aggiunta del widget o al reboot.
        // Mostriamo lo stato placeholder finché il Service non manda un aggiornamento.
        appWidgetIds.forEach { id ->
            updateWidget(context, appWidgetManager, id, WidgetState())
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            // Azioni pulsanti — le giriamo al Service
            ACTION_PLAY_PAUSE,
            ACTION_NEXT,
            ACTION_PREVIOUS,
            ACTION_SHUFFLE,
            ACTION_REPEAT -> {
                val serviceIntent = Intent(context, PlayerNotificationService::class.java)
                    .setAction(intent.action)
                context.startService(serviceIntent)
            }

            // Aggiornamento dello stato inviato dal Service
            ACTION_UPDATE_WIDGET -> {
                val state = WidgetState(
                    title          = intent.getStringExtra(EXTRA_TITLE)   ?: "Nessun brano",
                    artist         = intent.getStringExtra(EXTRA_ARTIST)  ?: "",
                    isPlaying      = intent.getBooleanExtra(EXTRA_IS_PLAYING, false),
                    progressMax    = intent.getIntExtra(EXTRA_PROGRESS_MAX, 1000),
                    progress       = intent.getIntExtra(EXTRA_PROGRESS, 0),
                    shuffleEnabled = intent.getBooleanExtra(EXTRA_SHUFFLE, false),
                    repeatEnabled  = intent.getBooleanExtra(EXTRA_REPEAT, false),
                    albumArt = WidgetArtCache.bitmap
                )
                val manager = AppWidgetManager.getInstance(context)
                val ids     = manager.getAppWidgetIds(
                    android.content.ComponentName(context, PlayerWidget::class.java)
                )
                ids.forEach { id -> updateWidget(context, manager, id, state) }
            }
        }
    }

    companion object {
        // Actions widget → Service
        const val ACTION_PLAY_PAUSE    = "com.example.mp3player.widget.PLAY_PAUSE"
        const val ACTION_NEXT          = "com.example.mp3player.widget.NEXT"
        const val ACTION_PREVIOUS      = "com.example.mp3player.widget.PREVIOUS"
        const val ACTION_SHUFFLE       = "com.example.mp3player.widget.SHUFFLE"
        const val ACTION_REPEAT        = "com.example.mp3player.widget.REPEAT"

        // Action Service → widget
        const val ACTION_UPDATE_WIDGET = "com.example.mp3player.widget.UPDATE"

        // Extra per ACTION_UPDATE_WIDGET
        const val EXTRA_TITLE          = "title"
        const val EXTRA_ARTIST         = "artist"
        const val EXTRA_IS_PLAYING     = "is_playing"
        const val EXTRA_PROGRESS       = "progress"
        const val EXTRA_PROGRESS_MAX   = "progress_max"
        const val EXTRA_SHUFFLE        = "shuffle"
        const val EXTRA_REPEAT         = "repeat"
        const val EXTRA_ALBUM_ART      = "album_art"

        fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            state: WidgetState
        ) {
            val currentSec = state.progress * (state.progressMax / 1000) / 1000

            val isDark = context
                .getSharedPreferences("splash_prefs", Context.MODE_PRIVATE)
                .getBoolean("dark_theme_enabled", true)

            // Colori tema
            val bgColor      = if (isDark) Color.parseColor("#FF121218") else Color.parseColor("#FFFFFFFF")
            val textColor    = if (isDark) Color.parseColor("#FFF8F9FA") else Color.parseColor("#FF1A1A24")
            val subTextColor = if (isDark) Color.parseColor("#99F8F9FA") else Color.parseColor("#991A1A24")
            val accentColor = if (isDark) Color.parseColor("#FF1565C0") else Color.parseColor("#FFA5D9FD")

            val views = RemoteViews(context.packageName, R.layout.widget_player)

            // Sfondo
            views.setInt(R.id.widget_root, "setBackgroundColor", bgColor)

            // Testi e colori
            views.setTextViewText(R.id.widget_title,  state.title)
            views.setTextViewText(R.id.widget_artist, state.artist)
            views.setTextColor(R.id.widget_title,  textColor)
            views.setTextColor(R.id.widget_artist, subTextColor)

            // Progress bar
            views.setInt(R.id.widget_progress, "setMax",      state.progressMax)
            views.setInt(R.id.widget_progress, "setProgress", state.progress)
            // SOSTITUISCI con:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // API 31+: metodo dedicato RemoteViews per ColorStateList
                views.setColorStateList(R.id.widget_progress, "setProgressTintList",
                    android.content.res.ColorStateList.valueOf(accentColor))
            } else {
                // Fallback: imposta il colore direttamente come Int tramite reflection
                views.setInt(R.id.widget_progress, "setProgressBackgroundTintMode",
                    android.graphics.PorterDuff.Mode.SRC_IN.ordinal)
            }

            // Copertina
            if (state.albumArt != null) {
                views.setImageViewBitmap(R.id.widget_cover, state.albumArt)
            } else {
                views.setImageViewResource(R.id.widget_cover, R.drawable.outline_music_note_24)
            }

            // Icona play/pausa
            val playIcon = if (state.isPlaying) R.drawable.outline_pause_24 else R.drawable.outline_play_arrow_24
            views.setImageViewResource(R.id.widget_btn_play_pause, playIcon)

            // Tint shuffle e repeat in base allo stato attivo
            val shuffleTint = if (state.shuffleEnabled) accentColor else subTextColor
            val repeatTint  = if (state.repeatEnabled)  accentColor else subTextColor
            views.setInt(R.id.widget_btn_shuffle, "setColorFilter", shuffleTint)
            views.setInt(R.id.widget_btn_repeat,  "setColorFilter", repeatTint)
            views.setInt(R.id.widget_btn_prev,    "setColorFilter", textColor)
            views.setInt(R.id.widget_btn_next,    "setColorFilter", textColor)
            views.setInt(R.id.widget_btn_play_pause, "setColorFilter", textColor)

            // PendingIntent pulsanti
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, widgetActionIntent(context, ACTION_PLAY_PAUSE, 20))
            views.setOnClickPendingIntent(R.id.widget_btn_next,       widgetActionIntent(context, ACTION_NEXT,       21))
            views.setOnClickPendingIntent(R.id.widget_btn_prev,       widgetActionIntent(context, ACTION_PREVIOUS,   22))
            views.setOnClickPendingIntent(R.id.widget_btn_shuffle,    widgetActionIntent(context, ACTION_SHUFFLE,    23))
            views.setOnClickPendingIntent(R.id.widget_btn_repeat,     widgetActionIntent(context, ACTION_REPEAT,     24))

            // Tocco sul widget → apre l'app
            val openApp = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_cover, openApp)
            views.setOnClickPendingIntent(R.id.widget_title, openApp)

            manager.updateAppWidget(widgetId, views)
        }

        private fun widgetActionIntent(context: Context, action: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, PlayerWidget::class.java).setAction(action)
            return PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}

data class WidgetState(
    val title: String          = "Nessun brano",
    val artist: String         = "",
    val isPlaying: Boolean     = false,
    val progress: Int          = 0,
    val progressMax: Int       = 1000,
    val shuffleEnabled: Boolean = false,
    val repeatEnabled: Boolean  = false,
    val albumArt: Bitmap?      = null
)