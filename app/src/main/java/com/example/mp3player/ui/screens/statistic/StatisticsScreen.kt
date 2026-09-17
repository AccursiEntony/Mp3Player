package com.example.mp3player.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mp3player.data.model.SongPlayStat
import com.example.mp3player.ui.components.CoverArtImage
import com.example.mp3player.ui.theme.AppColors
import com.example.mp3player.ui.theme.AppTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onClose: () -> Unit
) {
    val colors = AppTheme.colors
    val stats by viewModel.playStats.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Svuotare la cronologia?") },
            text = { Text("Tutti i conteggi di ascolto verranno eliminati definitivamente.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onClearHistory()
                    showClearConfirm = false
                }) { Text("Svuota", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Annulla") }
            }
        )
    }

    if (stats.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bgMainViews),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = colors.content.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Nessuna statistica disponibile",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.content.copy(alpha = 0.6f)
                )
                Text(
                    text = "Ascolta un brano per iniziare a vedere le statistiche",
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
            bottom = 80.dp,
            start = 12.dp,
            end = 12.dp,
            top = 8.dp
        )
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stats.size} brani ascoltati",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.content.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showClearConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Svuota cronologia", tint = colors.content.copy(alpha = 0.7f))
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Chiudi", tint = colors.content.copy(alpha = 0.7f))
                }
            }
        }

        itemsIndexed(
            items = stats,
            key = { _, stat -> stat.song.id }
        ) { index, stat ->
            StatisticSongItem(
                rank = index + 1,
                stat = stat,
                isPlaying = playbackState.currentSong?.id == stat.song.id,
                colors = colors,
                onClick = { viewModel.onSongClick(stat) }
            )
        }
    }
}

@Composable
private fun StatisticSongItem(
    rank: Int,
    stat: SongPlayStat,
    isPlaying: Boolean,
    colors: AppColors,
    onClick: () -> Unit
) {
    // Solo i primi 3 hanno un colore dedicato (oro/argento/bronzo), gli altri restano neutri
    val medalColor: Color? = when (rank) {
        1 -> colors.podiumGold
        2 -> colors.podiumSilver
        3 -> colors.podiumBronze
        else -> null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isPlaying) Modifier.background(colors.selectedBg.copy(alpha = 0.15f))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Badge di posizione: cerchio colorato per il podio, numero neutro per gli altri
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .then(
                    if (medalColor != null) {
                        Modifier
                            .background(medalColor.copy(alpha = 0.18f))
                            .border(1.5.dp, medalColor, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = medalColor ?: colors.content.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        CoverArtImage(
            albumArtUri = stat.song.albumArtUri,
            size = 44.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stat.song.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stat.song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = colors.content.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = null,
                tint = medalColor ?: colors.content.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stat.playCount.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = medalColor ?: colors.content
            )
        }
    }
}