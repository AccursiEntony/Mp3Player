package com.example.mp3player.data.repository

import com.example.mp3player.data.model.Song
import kotlinx.coroutines.flow.StateFlow

interface HiddenSongsRepository {
    /** Set reattivo degli ID nascosti (stringhe per compatibilità DataStore). */
    val hiddenIds: StateFlow<Set<Long>>

    fun hide(song: Song)
    fun unhide(song: Song)
    fun isHidden(song: Song): Boolean
}