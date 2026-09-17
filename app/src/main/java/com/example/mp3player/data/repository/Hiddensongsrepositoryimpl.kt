package com.example.mp3player.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.mp3player.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// DataStore dedicato ai brani nascosti (separato da user_preferences per chiarezza)
private val Context.hiddenSongsDataStore by preferencesDataStore(name = "hidden_songs")

class HiddenSongsRepositoryImpl(private val context: Context) : HiddenSongsRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val KEY_HIDDEN_IDS = stringSetPreferencesKey("hidden_song_ids")

    private val _hiddenIds = MutableStateFlow<Set<Long>>(emptySet())
    override val hiddenIds: StateFlow<Set<Long>> = _hiddenIds.asStateFlow()

    init {
        // Carica gli ID nascosti salvati all'avvio
        scope.launch {
            context.hiddenSongsDataStore.data.collect { prefs ->
                val ids = prefs[KEY_HIDDEN_IDS]
                    ?.mapNotNull { it.toLongOrNull() }
                    ?.toSet()
                    ?: emptySet()
                _hiddenIds.value = ids
            }
        }
    }

    override fun hide(song: Song) {
        val newSet = _hiddenIds.value + song.id
        _hiddenIds.value = newSet
        persist(newSet)
    }

    override fun unhide(song: Song) {
        val newSet = _hiddenIds.value - song.id
        _hiddenIds.value = newSet
        persist(newSet)
    }

    override fun isHidden(song: Song): Boolean = song.id in _hiddenIds.value

    private fun persist(ids: Set<Long>) {
        scope.launch {
            context.hiddenSongsDataStore.edit { prefs ->
                prefs[KEY_HIDDEN_IDS] = ids.map { it.toString() }.toSet()
            }
        }
    }
}