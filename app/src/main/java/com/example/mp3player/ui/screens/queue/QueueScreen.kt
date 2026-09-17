package com.example.mp3player.ui.screens.queue

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.mp3player.ui.components.QueueItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import com.example.mp3player.ui.components.RowIconButton
import com.example.mp3player.ui.theme.AppTheme
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.foundation.layout.Column

@Composable
fun QueueScreen(viewModel: QueueViewModel) {

    val colors = AppTheme.colors
    val queue         by viewModel.queue.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val favorites      by viewModel.favorites.collectAsState()

    val listState = rememberLazyListState()
    var draggingSongId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetY     by remember { mutableStateOf(0f) }

    if (queue.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(colors.bgMainViews),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = colors.content.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "La coda è vuota",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.content.copy(alpha = 0.6f)
                )
                Text(
                    text = "Scorri un brano verso destra per aggiungerlo",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.content.copy(alpha = 0.4f)
                )
            }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().background(colors.bgMainViews),
        contentPadding = PaddingValues(
            bottom = 80.dp   // spazio sotto l'ultima canzone
        )
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val countText = remember(queue.size) {
                    if (queue.size == 1) "1 brano" else "${queue.size} brani"
                }
                Text(
                    text = countText,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.content.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { viewModel.onClearQueue() }
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Svuota coda",
                        tint = colors.content
                    )
                }
            }
        }

        itemsIndexed(queue, key = { _, song -> song.id }) { index, song ->
            val isDragging = draggingSongId == song.id

            val durationText = remember(song.duration) { formatDuration(song.duration) }
            val isFavorite = remember(favorites, song.id) { favorites.any { it.id == song.id } }
            val isPlaying = remember(playbackState.currentSong?.id, song.id) { playbackState.currentSong?.id == song.id }

            QueueItem(
                song             = song,
                index            = index,
                queueSize        = queue.size,
                isPlaying        = isPlaying,
                isFavorite       = isFavorite,
                isDragging       = isDragging,
                durationText     = durationText,
                onClick          = { viewModel.onSongClick(song) },
                onFavoriteToggle = { viewModel.onFavoriteToggle(song) },
                modifier         = Modifier.graphicsLayer {
                    translationY = if (isDragging) dragOffsetY else 0f
                },
                dragHandleModifier = Modifier.pointerInput(song.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            draggingSongId = song.id
                            dragOffsetY = 0f
                        },
                        onDragEnd = {
                            draggingSongId = null
                            dragOffsetY = 0f
                        },
                        onDragCancel = {
                            draggingSongId = null
                            dragOffsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetY += dragAmount.y

                            val currentIndex = queue.indexOfFirst { it.id == song.id }
                            if (currentIndex < 0) return@detectDragGesturesAfterLongPress

                            val itemHeight = listState.layoutInfo.visibleItemsInfo
                                .firstOrNull { it.key == song.id }
                                ?.size
                                ?.takeIf { it > 0 }
                                ?: return@detectDragGesturesAfterLongPress

                            if (dragOffsetY > itemHeight / 2f && currentIndex < queue.lastIndex) {
                                viewModel.onMove(currentIndex, currentIndex + 1)
                                dragOffsetY -= itemHeight
                            } else if (dragOffsetY < -itemHeight / 2f && currentIndex > 0) {
                                viewModel.onMove(currentIndex, currentIndex - 1)
                                dragOffsetY += itemHeight
                            }
                        }
                    )
                }
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val total   = ms / 1000
    val minutes = total / 60
    val seconds = total % 60
    return "%d:%02d".format(minutes, seconds)
}