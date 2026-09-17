package com.example.mp3player.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Classe di ogni elemento nella coda salvato*/
@Entity(tableName = "queue")
data class QueueItemEntity(
    @PrimaryKey val position: Int,
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val folderPath: String,
    val trackNumber: Int,
    val dateAdded: Long,
    val albumArtUri: String?,
    val year: Int?
)