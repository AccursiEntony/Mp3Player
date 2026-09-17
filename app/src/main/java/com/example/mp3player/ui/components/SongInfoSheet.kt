package com.example.mp3player.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.mp3player.data.model.Song
import com.example.mp3player.ui.theme.AppTheme
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongInfoSheet(
    song: Song,
    isFavorite: Boolean,
    isHidden: Boolean = false,
    playCount: Int? = null,
    onDismiss: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToQueue: () -> Unit,
    onHideSong: (Song) -> Unit = {},
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.menuInfoBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CoverArtImage(
                    albumArtUri = song.albumArtUri,
                    size = 200.dp,
                    cornerRadius = 12.dp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = song.title,
                    color = colors.content,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(label = "Artista", value = song.artist,  colors = colors)
                    DetailRow(label = "Album",   value = song.album,   colors = colors)
                    DetailRow(
                        label = "Anno",
                        value = song.year?.takeIf { it > 0 }?.toString() ?: "—",
                        colors = colors
                    )
                    DetailRow(
                        label = "Durata",
                        value = formatDurationFull(song.duration),
                        colors = colors
                    )
                    DetailRow(
                        label = "Ascolti",
                        value = playCount?.takeIf { it > 0 }?.toString() ?: "—",
                        colors = colors
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colors.content.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(8.dp))
            }

//            AnimatedVisibility(
//                visible = !isExpanded,
//                enter = fadeIn() + expandVertically(),
//                exit = fadeOut() + shrinkVertically()
//            ) {
//                Column(modifier = Modifier.fillMaxWidth()) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        CoverArtImage(
//                            albumArtUri = song.albumArtUri,
//                            size = 56.dp,
//                            cornerRadius = 8.dp
//                        )
//
//                        Spacer(modifier = Modifier.width(14.dp))
//
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = song.title,
//                                color = colors.content,
//                                fontSize = 15.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                            Text(
//                                text = song.artist,
//                                color = colors.content.copy(alpha = 0.6f),
//                                fontSize = 12.sp,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                        }
//                    }
//                    HorizontalDivider(color = colors.content.copy(alpha = 0.08f))
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//            }

            SheetAction(
                icon = if (isFavorite) Icons.Default.HeartBroken else Icons.Rounded.FavoriteBorder,
                label = if (isFavorite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                tint = if (isHidden) colors.content.copy(alpha = 0.3f)
                else if (isFavorite) Color(0xFFE05555)
                else colors.content,
                onClick = { if (!isHidden) { onFavoriteToggle(); onDismiss() } }
            )

            SheetAction(
                icon = Icons.Default.QueueMusic,
                label = "Aggiungi alla coda",
                tint = if (isHidden) colors.content.copy(alpha = 0.3f) else colors.content,
                onClick = { if (!isHidden) { onAddToQueue(); onDismiss() } }
            )

            SheetAction(
                icon = Icons.Default.Share,
                label = "Condividi",
                tint = colors.content,
                onClick = {
                    val file = File(song.path)
                    val uri: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "audio/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, song.title))
                    onDismiss()
                }
            )

            SheetAction(
                icon = if (isHidden) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                label = if (isHidden) "Mostra brano" else "Nascondi brano",
                tint = colors.content,
                onClick = { onHideSong(song); onDismiss() }
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    colors: com.example.mp3player.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colors.content.copy(alpha = 0.5f),
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = colors.content,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun SheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 14.sp
        )
    }
}

private fun formatDurationFull(ms: Long): String {
    val total = ms / 1000
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}