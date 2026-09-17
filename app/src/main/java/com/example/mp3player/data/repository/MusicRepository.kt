package com.example.mp3player.data.repository

import com.example.mp3player.data.model.MusicFolder

interface MusicRepository {
    fun loadFolder(rootPath: String): MusicFolder
    fun loadFolderFromUri(uriString: String): MusicFolder?
}