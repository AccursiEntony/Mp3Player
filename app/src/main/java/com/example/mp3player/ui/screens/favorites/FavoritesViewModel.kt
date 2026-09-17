package com.example.mp3player.ui.screens.favorites

import androidx.lifecycle.ViewModel
import com.example.mp3player.data.model.Song
import com.example.mp3player.data.repository.FavoritesRepository
import com.example.mp3player.data.repository.PlayStatsRepository
import com.example.mp3player.data.repository.QueueRepository
import com.example.mp3player.player.Media3PlayerController
import com.example.mp3player.player.PlaybackState
import kotlinx.coroutines.flow.StateFlow


class FavoritesViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val queueRepository: QueueRepository,
    private val playerController: Media3PlayerController,
    private val playStatsRepository: PlayStatsRepository
) : ViewModel() {

    val favorites: StateFlow<List<Song>>        = favoritesRepository.favorites
    val playbackState: StateFlow<PlaybackState> = playerController.state

    fun getPlayCount(song: Song): Int? =
        playStatsRepository.playStats.value
            .find { it.song.id == song.id }
            ?.playCount
            ?.takeIf { it > 0 }
    fun onSongClick(song: Song) {
        val queue = favoritesRepository.favorites.value
        playerController.play(song, queue)
    }

    fun onRemoveFromFavorites(song: Song) {
        favoritesRepository.remove(song)
    }

    fun onAddToQueue(song: Song) {
        queueRepository.add(song)
    }
}