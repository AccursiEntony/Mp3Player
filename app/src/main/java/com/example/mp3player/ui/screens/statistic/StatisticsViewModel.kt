package com.example.mp3player.ui.screens.statistics

import androidx.lifecycle.ViewModel
import com.example.mp3player.data.model.SongPlayStat
import com.example.mp3player.data.repository.PlayStatsRepository
import com.example.mp3player.player.Media3PlayerController
import com.example.mp3player.player.PlaybackState
import kotlinx.coroutines.flow.StateFlow

class StatisticsViewModel(
    private val playStatsRepository: PlayStatsRepository,
    private val playerController: Media3PlayerController,
) : ViewModel() {

    val playStats: StateFlow<List<SongPlayStat>> = playStatsRepository.playStats
    val playbackState: StateFlow<PlaybackState>  = playerController.state

    fun onSongClick(stat: SongPlayStat) {
        val queue = playStatsRepository.playStats.value.map { it.song }
        playerController.play(stat.song, queue)
    }

    fun onClearHistory() = playStatsRepository.clearHistory()
}