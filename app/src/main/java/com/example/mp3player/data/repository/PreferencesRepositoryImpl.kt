package com.example.mp3player.data.repository

import com.example.mp3player.data.local.datastore.UserPreferences
import com.example.mp3player.data.local.datastore.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow

class PreferencesRepositoryImpl(
    private val dataStore: UserPreferencesDataStore,
    private val context: android.content.Context
) : PreferencesRepository {


    override val preferences: Flow<UserPreferences> = dataStore.preferences

    override suspend fun saveShuffle(enabled: Boolean): UserPreferences {
        return dataStore.saveShuffle(enabled) // Assicurati che dataStore restituisca UserPreferences
    }

    override suspend fun saveRepeat(enabled: Boolean): UserPreferences {
        return dataStore.saveRepeat(enabled)
    }

    // Rinominato posMs in positionMs per allinearsi all'interfaccia
    override suspend fun saveCurrentSong(songId: Long, positionMs: Long): UserPreferences {
        return dataStore.saveCurrentSong(songId, positionMs)
    }

    override suspend fun savePosition(positionMs: Long): UserPreferences {
        return dataStore.savePosition(positionMs)
    }

    // Rinominati path -> folderPath e sel -> selection per allinearsi all'interfaccia
    override suspend fun saveFolderAndSelection(folderPath: String, selection: String): UserPreferences {
        return dataStore.saveFolderAndSelection(folderPath, selection)
    }

    override suspend fun saveDarkTheme(enabled: Boolean): UserPreferences {
        context.getSharedPreferences("splash_prefs", android.content.Context.MODE_PRIVATE)
            .edit().putBoolean("dark_theme_enabled", enabled).apply()
        return dataStore.saveDarkTheme(enabled)
    }

    override suspend fun saveTechnicalPlayer(
        enabled: Boolean
    ): UserPreferences {
        return dataStore.saveTechnicalPlayer(enabled)
    }
}
