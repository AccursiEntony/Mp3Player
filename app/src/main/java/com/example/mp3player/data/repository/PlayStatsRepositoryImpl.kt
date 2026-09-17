package com.example.mp3player.data.repository

import com.example.mp3player.data.local.database.dao.PlayCountDao
import com.example.mp3player.data.local.database.entity.PlayCountEntity
import com.example.mp3player.data.model.Song
import com.example.mp3player.data.model.SongPlayStat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayStatsRepositoryImpl(
    private val dao: PlayCountDao
) : PlayStatsRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val playStats: StateFlow<List<SongPlayStat>> = dao.observeAll()
        .map { list -> list.map { it.toStat() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun registerPlay(song: Song) {
        scope.launch {
            dao.registerPlay(song.toEntity(), System.currentTimeMillis())
        }
    }

    override fun clearHistory() {
        scope.launch { dao.clearAll() }
    }}

// ── Mapper ────────────────────────────────────────────────────────────────────

private fun PlayCountEntity.toStat() = SongPlayStat(
    song = Song(
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
        year        = year
    ),
    playCount    = count,
    lastPlayedAt = lastPlayedAt
)

private fun Song.toEntity() = PlayCountEntity(
    id           = id,
    title        = title,
    artist       = artist,
    album        = album,
    duration     = duration,
    path         = path,
    folderPath   = folderPath,
    trackNumber  = trackNumber,
    dateAdded    = dateAdded,
    albumArtUri  = albumArtUri,
    year         = year,
    count        = 0,   // ignorato se il brano esiste già: vedi PlayCountDao.registerPlay
    lastPlayedAt = 0L    // idem
)