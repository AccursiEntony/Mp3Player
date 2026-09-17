package com.example.mp3player.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Traccia quante volte ogni brano è stato ascoltato, per le statistiche. */
@Entity(tableName = "play_counts")
data class PlayCountEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val folderPath: String,
    val trackNumber: Int,
    val dateAdded: Long,
    val albumArtUri: String?,
    val year: Int?,
    val count: Int,
    val lastPlayedAt: Long
)