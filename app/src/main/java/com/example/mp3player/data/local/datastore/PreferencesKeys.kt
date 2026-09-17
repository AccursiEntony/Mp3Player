package com.example.mp3player.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Chiavi di lettura e scrittura per Jetpack DataStore.
 * Centralizza tutte le preferenze di configurazione dell'applicazione.
 */
object PreferencesKeys {

    // Impostazioni dello stato del player musicale
    val SHUFFLE_ENABLED     = booleanPreferencesKey("shuffle_enabled")
    val REPEAT_ENABLED      = booleanPreferencesKey("repeat_enabled")
    val CURRENT_SONG_ID     = longPreferencesKey("current_song_id")
    val CURRENT_POSITION_MS = longPreferencesKey("current_position_ms")

    // Gestione della cartella corrente e selezione dei file (Usa il PATH anziché l'URI)
    val CURRENT_FOLDER_PATH = stringPreferencesKey("current_folder_path")
    val CURRENT_SELECTION   = stringPreferencesKey("current_selection")

    // Personalizzazione grafica dell'applicazione
    val DARK_THEME_ENABLED  = booleanPreferencesKey("dark_theme_enabled")

    val TECHNICAL_PLAYER_ENABLED = booleanPreferencesKey("technical_player_enabled")
}
