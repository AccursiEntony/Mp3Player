package com.example.mp3player.data.model

/** Rappresenta una cartella musicale principale selezionata dall'utente. */
data class MusicFolder(
    val path: String,
    val name: String,
    val directSongs: List<Song>,
    val subFolders: List<SubMusicFolder>
)

/** Rappresenta una sottocartella contenuta all'interno della cartella principale. */
data class SubMusicFolder(
    val path: String,
    val name: String,
    val songs: List<Song>
)
