package com.example.mp3player.data.repository

import com.example.mp3player.data.model.Song
import com.example.mp3player.data.model.SongPlayStat
import kotlinx.coroutines.flow.StateFlow

interface PlayStatsRepository {
    /** Statistiche dei brani ascoltati almeno una volta, ordinate dal più ascoltato. */
    val playStats: StateFlow<List<SongPlayStat>>

    /** Da chiamare ogni volta che un brano inizia la riproduzione. */
    fun registerPlay(song: Song)

    /** Svuota completamente la cronologia degli ascolti. */
    fun clearHistory()
}