package com.example.mp3player.data.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.example.mp3player.data.mediastore.MediaStoreAudioSource
import com.example.mp3player.data.model.MusicFolder
import com.example.mp3player.data.model.Song
import com.example.mp3player.data.model.SubMusicFolder
import java.io.File

class MusicRepositoryImpl(private val context: Context) : MusicRepository {

    private val source = MediaStoreAudioSource(context)

    override fun loadFolder(rootPath: String): MusicFolder {
        val allSongs    = source.querySongsUnderPath(rootPath)
        val directSongs = allSongs.filter { it.folderPath == rootPath }
        val subMap      = allSongs.filter { it.folderPath != rootPath }
            .groupBy { firstSubFolder(rootPath, it.folderPath) }
        val subFolders  = subMap.entries.sortedBy { it.key }.map { (subPath, songs) ->
            SubMusicFolder(path = subPath, name = File(subPath).name, songs = songs)
        }
        return MusicFolder(
            path        = rootPath,
            name        = File(rootPath).name,
            directSongs = directSongs,
            subFolders  = subFolders
        )
    }

    override fun loadFolderFromUri(uriString: String): MusicFolder? {
        val realPath = uriToPath(uriString)
        if (realPath == null) return null
        return loadFolder(realPath)
    }

    // content://com.android.externalstorage.documents/tree/primary:Music/Sub
    // → /storage/emulated/0/Music/Sub
    private fun uriToPath(uriString: String): String? {
        return try {
            val uri   = Uri.parse(uriString)
            val docId = DocumentsContract.getTreeDocumentId(uri)
            val parts = docId.split(":")
            if (parts.size < 2) return null
            val volume = parts[0]
            val rel    = parts[1]
            val base   = if (volume == "primary") "/storage/emulated/0" else "/storage/$volume"
            "$base/$rel"
        } catch (_: Exception) { null }
    }

    private fun firstSubFolder(rootPath: String, songFolderPath: String): String {
        val relative     = songFolderPath.removePrefix(rootPath).trimStart('/')
        val firstSegment = relative.split('/').first()
        return "$rootPath/$firstSegment"
    }


}