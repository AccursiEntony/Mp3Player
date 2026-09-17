package com.example.mp3player.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mp3player.data.model.Song
import com.example.mp3player.ui.theme.AppTheme

@Composable
fun QueueItem(
    song: Song,
    index: Int,
    queueSize: Int,
    isPlaying: Boolean,
    isFavorite: Boolean,
    durationText: String,
    isDragging: Boolean = false,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    // Lo sfondo diventa colors.selectedBorder sia per isDragging che per isPlaying
    val bgColor by animateColorAsState(
        targetValue = when {
            isDragging || isPlaying -> colors.selectedBorder
            else                    -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "QueueBg"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverArtImage(albumArtUri = song.albumArtUri, size = 40.dp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.content,
                fontWeight = if (isPlaying || isDragging) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (song.artist.isNotBlank()) {
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.content.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = durationText,
            style = MaterialTheme.typography.labelSmall,
            color = colors.content.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Trascina per riordinare",
            tint = colors.content.copy(alpha = 0.6f),
            modifier = dragHandleModifier.padding(horizontal = 8.dp)
        )

        RowIconButton(
            icon = Icons.Default.Favorite,
            contentDescription = "Preferito",
            active = isFavorite,
            colors = colors,
            onClick = onFavoriteToggle
        )
    }
}
