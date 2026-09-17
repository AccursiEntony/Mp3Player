package com.example.mp3player.player

import com.example.mp3player.data.model.Song

/** Tutti gli stati e le funzioni del player della canzone. */

interface PlayerController {
    fun play(song: Song, queue: List<Song>)
    fun pause()
    fun togglePlayPause()
    fun skipNext()
    fun skipPrevious()
    fun seekTo(positionMs: Long)
    fun setShuffleEnabled(enabled: Boolean, scope: List<Song>)
    fun setRepeatEnabled(enabled: Boolean)
    fun release()
}