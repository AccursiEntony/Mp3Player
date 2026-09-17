package com.example.mp3player.data.model

/** Rappresenta la classe di una canzone.  */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,        // millisecondi
    val path: String,          // path assoluto del file
    val folderPath: String,    // path della cartella padre
    val trackNumber: Int,
    val dateAdded: Long,
    val albumArtUri: String?,
    val year: Int?
)
