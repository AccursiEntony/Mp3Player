package com.example.mp3player.data.repository

import com.example.mp3player.data.local.database.dao.FavoriteDao
import com.example.mp3player.data.local.database.entity.FavoriteEntity
import com.example.mp3player.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesRepositoryImpl(
    private val dao: FavoriteDao
) : FavoritesRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val favorites: StateFlow<List<Song>> = dao.observeAll()
        .map { list -> list.map { it.toSong() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun add(song: Song) {
        scope.launch { dao.insert(song.toEntity()) }
    }

    override fun remove(song: Song) {
        scope.launch { dao.deleteById(song.id) }
    }

    override fun isFavorite(song: Song): Boolean =
        favorites.value.any { it.id == song.id }
}

// ── Mapper ────────────────────────────────────────────────────────────────────

private fun FavoriteEntity.toSong() = Song(
    id          = id,
    title       = title,
    artist      = artist,
    album       = album,
    duration    = duration,
    path        = path,
    folderPath  = folderPath,
    trackNumber = trackNumber,
    dateAdded   = dateAdded,
    albumArtUri = albumArtUri,
    year = year
)

private fun Song.toEntity() = FavoriteEntity(
    id          = id,
    title       = title,
    artist      = artist,
    album       = album,
    duration    = duration,
    path        = path,
    folderPath  = folderPath,
    trackNumber = trackNumber,
    dateAdded   = dateAdded,
    albumArtUri = albumArtUri,
    year = year
)