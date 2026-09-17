package com.example.mp3player.data.repository

import com.example.mp3player.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    val preferences: Flow<UserPreferences>

    suspend fun saveShuffle(enabled: Boolean): UserPreferences

    suspend fun saveRepeat(enabled: Boolean): UserPreferences

    suspend fun saveCurrentSong(
        songId: Long,
        positionMs: Long
    ): UserPreferences

    suspend fun savePosition(
        positionMs: Long
    ): UserPreferences

    suspend fun saveFolderAndSelection(
        folderPath: String,
        selection: String
    ): UserPreferences

    suspend fun saveDarkTheme(enabled: Boolean): UserPreferences

    suspend fun saveTechnicalPlayer(enabled: Boolean): UserPreferences
}