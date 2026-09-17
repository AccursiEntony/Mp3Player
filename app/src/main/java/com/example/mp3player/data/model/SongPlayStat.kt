package com.example.mp3player.data.model

/** Rappresenta le statistiche di ascolto di un singolo brano. */
data class SongPlayStat(
    val song: Song,
    val playCount: Int,
    val lastPlayedAt: Long
)