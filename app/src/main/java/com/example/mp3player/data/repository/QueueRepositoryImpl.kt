package com.example.mp3player.data.repository

import com.example.mp3player.data.local.database.dao.QueueDao
import com.example.mp3player.data.local.database.entity.QueueItemEntity
import com.example.mp3player.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QueueRepositoryImpl(
    private val dao: QueueDao
) : QueueRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    override val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    init {
        // Carica la coda salvata all'avvio
        scope.launch {
            val saved = dao.getAll().map { it.toSong() }
            _queue.value = saved
        }
    }

    override fun setQueue(songs: List<Song>) {
        _queue.value = songs
        persist(songs)
    }

    override fun add(song: Song) {
        _queue.update { list ->
            val withoutDuplicate = list.filter { it.id != song.id }
            listOf(song) + withoutDuplicate
        }
        persist(_queue.value)
    }

    override fun moveUp(index: Int) {
        if (index <= 0) return
        _queue.update { it.toMutableList().also { l -> l.add(index - 1, l.removeAt(index)) } }
        persist(_queue.value)
    }

    override fun moveDown(index: Int) {
        _queue.update { list ->
            if (index >= list.lastIndex) return@update list
            list.toMutableList().also { l -> l.add(index + 1, l.removeAt(index)) }
        }
        persist(_queue.value)
    }

    override fun move(from: Int, to: Int) {
        _queue.update { list ->
            if (from !in list.indices || from == to) return@update list
            val target = to.coerceIn(0, list.lastIndex)
            list.toMutableList().also { l -> l.add(target, l.removeAt(from)) }
        }
        persist(_queue.value)
    }

    override fun removeAt(index: Int) {
        _queue.update { it.toMutableList().also { l -> l.removeAt(index) } }
        persist(_queue.value)
    }

    override fun popNext(): Song? {
        var popped: Song? = null
        _queue.update { list ->
            if (list.isEmpty()) return@update list
            popped = list.first()
            list.drop(1)
        }
        persist(_queue.value)
        return popped
    }

    override fun clear() {
        _queue.value = emptyList()
        scope.launch { dao.clearAll() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun persist(songs: List<Song>) {
        scope.launch {
            dao.clearAll()
            dao.insertAll(songs.mapIndexed { i, s -> s.toEntity(i) })
        }
    }
}

private fun QueueItemEntity.toSong() = Song(
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

private fun Song.toEntity(position: Int) = QueueItemEntity(
    position    = position,
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