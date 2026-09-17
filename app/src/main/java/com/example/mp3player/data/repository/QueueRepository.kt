package com.example.mp3player.data.repository

import com.example.mp3player.data.model.Song
import kotlinx.coroutines.flow.StateFlow

interface QueueRepository {
    val queue: StateFlow<List<Song>>
    fun setQueue(songs: List<Song>)
    fun add(song: Song)
    fun moveUp(index: Int)
    fun moveDown(index: Int)
    fun move(from: Int, to: Int)
    fun removeAt(index: Int)

    fun popNext(): Song?
    fun clear()
}