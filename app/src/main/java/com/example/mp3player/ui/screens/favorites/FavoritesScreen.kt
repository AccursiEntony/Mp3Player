package com.example.mp3player.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material3.Icon // ← Import aggiunto
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
import androidx.compose.ui.unit.dp
import com.example.mp3player.data.model.Song
import com.example.mp3player.ui.components.SongInfoSheet
import com.example.mp3player.ui.components.SwipeableSongItem
import com.example.mp3player.ui.theme.AppTheme

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onCloseSidebar: () -> Unit,
){
    val colors = AppTheme.colors

    val favorites by viewModel.favorites.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    var selectedSong by remember { mutableStateOf<Song?>(null) }

    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(colors.bgMainViews),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.HeartBroken,
                    contentDescription = null,
                    tint = colors.content.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Nessun preferito",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.content.copy(alpha = 0.6f)
                )
                Text(
                    text = "Scorri un brano verso sinistra per aggiungerlo",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.content.copy(alpha = 0.4f)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMainViews),

        contentPadding = PaddingValues(
            bottom = 80.dp,   // spazio sotto l'ultima canzone
            start = 12.dp,
            end = 12.dp
        )
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${favorites.size} brani",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.content.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        items(
            items = favorites,
            key = { it.id }
        ) { song ->
            FavoriteSongItem(
                song = song,
                isPlaying = playbackState.currentSong?.id == song.id,
                onClick = { viewModel.onSongClick(song) },
                onRemove = { viewModel.onRemoveFromFavorites(song) },
                onAddToQueue = { viewModel.onAddToQueue(song) },
                onLongClick = {
                    onCloseSidebar()
                    selectedSong = song
                }
            )
        }
    }
    selectedSong?.let { song ->
        SongInfoSheet(
            song             = song,
            isFavorite       = true,
            isHidden         = false,
            playCount        = viewModel.getPlayCount(song),
            onDismiss        = { selectedSong = null },
            onFavoriteToggle = { viewModel.onRemoveFromFavorites(song) },
            onAddToQueue     = { viewModel.onAddToQueue(song) },
            onHideSong       = {}
        )
    }
}

@Composable
private fun FavoriteSongItem(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onAddToQueue: () -> Unit,
    onLongClick: () -> Unit,
) {
    SwipeableSongItem(
        song = song,
        isPlaying = isPlaying,
        isFavorite = true,
        onClick = onClick,
        onFavoriteToggle = onRemove,
        onAddToQueue = onAddToQueue,
        favoriteLabel = "Rimuovi",
        favoriteIcon = Icons.Default.HeartBroken,
        staticIcon = Icons.Default.HeartBroken,
        onLongClick = onLongClick
    )
}
