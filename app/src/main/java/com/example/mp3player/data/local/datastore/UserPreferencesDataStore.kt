package com.example.mp3player.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

// Estensione per delegare l'inizializzazione del DataStore (Singleton implicito)
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

/** Modello di dati immutabile che rappresenta lo stato delle preferenze utente. */
data class UserPreferences(
    val shuffleEnabled: Boolean = false,
    val repeatEnabled: Boolean = false,
    val currentSongId: Long? = null,
    val currentPositionMs: Long = 0L,
    val currentFolderPath: String? = null,
    val currentSelection: String = "all",
    val darkThemeEnabled: Boolean = true,
    val technicalPlayerEnabled: Boolean = true
)

/** Gestore per la lettura e scrittura asincrona delle preferenze tramite Jetpack DataStore. */
class UserPreferencesDataStore(context: Context) {

    private val dataStore = context.dataStore

    // Flusso continuo (Flow) che emette i dati aggiornati a ogni modifica
    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            UserPreferences(
                shuffleEnabled    = prefs[PreferencesKeys.SHUFFLE_ENABLED] ?: false,
                repeatEnabled     = prefs[PreferencesKeys.REPEAT_ENABLED] ?: false,
                currentSongId     = prefs[PreferencesKeys.CURRENT_SONG_ID],
                currentPositionMs = prefs[PreferencesKeys.CURRENT_POSITION_MS] ?: 0L,
                currentFolderPath = prefs[PreferencesKeys.CURRENT_FOLDER_PATH],
                currentSelection  = prefs[PreferencesKeys.CURRENT_SELECTION] ?: "all",
                darkThemeEnabled  = prefs[PreferencesKeys.DARK_THEME_ENABLED] ?: true,
                technicalPlayerEnabled = prefs[PreferencesKeys.TECHNICAL_PLAYER_ENABLED] ?: true
            )
        }

    /* =========================================================================
       Sezione: Metodi di Scrittura (Suspend Functions)
       ========================================================================= */

    // Salva lo stato della riproduzione casuale
    suspend fun saveShuffle(enabled: Boolean): UserPreferences {
        dataStore.edit { it[PreferencesKeys.SHUFFLE_ENABLED] = enabled }
        return preferences.first()
    }

    // Salva lo stato della riproduzione ciclica
    suspend fun saveRepeat(enabled: Boolean): UserPreferences {
        dataStore.edit { it[PreferencesKeys.REPEAT_ENABLED] = enabled }
        return preferences.first()
    }

    // Salva l'ID del brano corrente insieme alla sua posizione temporale
    suspend fun saveCurrentSong(songId: Long, positionMs: Long): UserPreferences {
        dataStore.edit {
            it[PreferencesKeys.CURRENT_SONG_ID] = songId
            it[PreferencesKeys.CURRENT_POSITION_MS] = positionMs
        }
        return preferences.first()
    }

    // Aggiorna solo il punto in millisecondi in cui è arrivata la riproduzione
    suspend fun savePosition(positionMs: Long): UserPreferences {
        dataStore.edit { it[PreferencesKeys.CURRENT_POSITION_MS] = positionMs }
        return preferences.first()
    }

    // Salva la preferenza del tema scuro (Dark Theme)
    suspend fun saveDarkTheme(enabled: Boolean): UserPreferences {
        dataStore.edit { it[PreferencesKeys.DARK_THEME_ENABLED] = enabled }
        return preferences.first()
    }

    // Salva la cartella di lavoro corrente e la selezione attiva
    suspend fun saveFolderAndSelection(folderPath: String, selection: String): UserPreferences {
        dataStore.edit {
            it[PreferencesKeys.CURRENT_FOLDER_PATH] = folderPath
            it[PreferencesKeys.CURRENT_SELECTION]   = selection
        }
        return preferences.first()
    }

    suspend fun saveTechnicalPlayer(
        enabled: Boolean
    ): UserPreferences {
        dataStore.edit {
            it[PreferencesKeys.TECHNICAL_PLAYER_ENABLED] = enabled
        }

        return preferences.first()
    }
}
