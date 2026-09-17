package com.example.mp3player.ui.screens.queue

import androidx.lifecycle.ViewModel
import com.example.mp3player.data.model.Song
import com.example.mp3player.data.repository.FavoritesRepository
import com.example.mp3player.data.repository.QueueRepository
import com.example.mp3player.player.Media3PlayerController
import com.example.mp3player.player.PlaybackState
import kotlinx.coroutines.flow.StateFlow

class QueueViewModel(
    private val queueRepository: QueueRepository,
    private val favoritesRepository: FavoritesRepository,
    private val playerController: Media3PlayerController,
) : ViewModel() {

    val queue: StateFlow<List<Song>>            = queueRepository.queue
    val playbackState: StateFlow<PlaybackState> = playerController.state
    // Reattivo: la UI ricompone subito quando i preferiti cambiano.
    val favorites: StateFlow<List<Song>>        = favoritesRepository.favorites

    fun onSongClick(song: Song) {
        val queue = queueRepository.queue.value
        playerController.play(song, queue)
        queueRepository.setQueue(queue)
    }

    /** Usato dal drag & drop per riordinare la coda. */
    fun onMove(from: Int, to: Int) = queueRepository.move(from, to)
    fun onClearQueue()             = queueRepository.clear()

    fun onFavoriteToggle(song: Song) {
        if (favoritesRepository.isFavorite(song)) favoritesRepository.remove(song)
        else favoritesRepository.add(song)
    }

    fun isFavorite(song: Song): Boolean = favoritesRepository.isFavorite(song)
}