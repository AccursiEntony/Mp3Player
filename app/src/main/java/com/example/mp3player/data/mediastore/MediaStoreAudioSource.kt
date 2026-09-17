package com.example.mp3player.data.mediastore

import android.content.Context
import android.provider.MediaStore
import com.example.mp3player.data.model.Song
import java.io.File

/** Sorgente dati che interroga il MediaStore di Android per recuperare i file audio locali. */
class MediaStoreAudioSource(private val context: Context) {

    /** Cerca e restituisce tutti i brani musicali presenti all'interno di una cartella specifica. */
    fun querySongsUnderPath(rootPath: String): List<Song> {
        val songs = mutableListOf<Song>()

        // Configurazione della query al MediaStore
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.YEAR,
        )

        // Filtro di selezione: estrae solo i file il cui percorso inizia con rootPath
        val selection     = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("$rootPath%")
        val sortOrder     = MediaStore.Audio.Media.TITLE + " ASC"

        // Esecuzione della query tramite ContentResolver
        context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->

            // Recupero preventivo degli indici delle colonne per ottimizzare le prestazioni
            val idCol       = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol   = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol     = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val trackCol    = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dateCol     = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val albumIdCol  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)

            // Iterazione sui risultati del cursore
            while (cursor.moveToNext()) {
                val path = cursor.getString(dataCol) ?: continue
                val albumId = cursor.getLong(albumIdCol)

                // Mappatura del record del database nell'oggetto di dominio Song
                songs.add(
                    Song(
                        id          = cursor.getLong(idCol),
                        title       = cursor.getString(titleCol) ?: File(path).nameWithoutExtension,
                        artist      = cursor.getString(artistCol) ?: "<Unknown>",
                        album       = cursor.getString(albumCol) ?: "<Unknown>",
                        duration    = cursor.getLong(durationCol),
                        path        = path,
                        folderPath  = File(path).parent ?: rootPath,
                        trackNumber = cursor.getInt(trackCol),
                        dateAdded   = cursor.getLong(dateCol),
                        albumArtUri = "content://media/external/audio/albumart/$albumId",
                        year = cursor.getInt(yearCol),
                    )
                )
            }
        }

        return songs
    }
}
