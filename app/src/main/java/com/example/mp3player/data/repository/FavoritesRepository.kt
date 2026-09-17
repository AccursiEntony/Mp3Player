package com.example.mp3player.data.repository

import com.example.mp3player.data.model.Song
import kotlinx.coroutines.flow.StateFlow

interface FavoritesRepository {
    val favorites: StateFlow<List<Song>>
    fun add(song: Song)
    fun remove(song: Song)
    fun isFavorite(song: Song): Boolean
}