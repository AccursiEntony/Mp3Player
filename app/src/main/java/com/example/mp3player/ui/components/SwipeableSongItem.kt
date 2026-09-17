package com.example.mp3player.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mp3player.data.model.Song
import com.example.mp3player.ui.theme.AppTheme
import kotlin.math.abs
import kotlin.math.roundToInt

private val CONFIRM_THRESHOLD_DP = 96.dp
private val HINT_THRESHOLD_DP = 40.dp

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SwipeableSongItem(
    song: Song,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToQueue: () -> Unit,
    favoriteLabel: String = "Preferiti",
    favoriteIcon: ImageVector = Icons.Default.Favorite,
    staticIcon: ImageVector = Icons.Default.Favorite,
    isFavoriteEnabled: Boolean = true,
    swipeEnabled: Boolean = true
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    val density = LocalDensity.current
    val confirmThresholdPx = with(density) { CONFIRM_THRESHOLD_DP.toPx() }
    val hintThresholdPx = with(density) { HINT_THRESHOLD_DP.toPx() }
    val swipeSlopPx = LocalViewConfiguration.current.touchSlop

    var offsetX by remember { mutableFloatStateOf(0f) }
    var isResetting by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val progress = (abs(offsetX) / confirmThresholdPx).coerceIn(0f, 1f)

    val swipingLeft = offsetX < -hintThresholdPx
    val swipingRight = offsetX > hintThresholdPx

    val bgColor by animateColorAsState(
        targetValue = when {
            swipingRight -> colors.endProgressBar.copy(alpha = 0.06f + progress * 0.10f)
            swipingLeft -> colors.startProgessBar.copy(alpha = 0.06f + progress * 0.10f)
            else -> Color.Transparent
        },
        animationSpec = tween(120),
        label = "swipeBg"
    )

    val iconScale by animateFloatAsState(
        targetValue = when {
            swipingLeft || swipingRight -> 0.85f + progress * 0.15f
            else -> 0.75f
        },
        animationSpec = tween(120),
        label = "iconScale"
    )

    val iconAlpha by animateFloatAsState(
        targetValue = when {
            swipingLeft || swipingRight -> 0.65f + progress * 0.35f
            else -> 0f
        },
        animationSpec = tween(120),
        label = "iconAlpha"
    )

    val animatedOffset by animateFloatAsState(
        targetValue = if (isResetting) 0f else offsetX,
        animationSpec = tween(
            durationMillis = if (isResetting) 220 else 0
        ),
        finishedListener = {
            if (isResetting) isResetting = false
        },
        label = "offsetReset"
    )

    var showConfirmation by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state           = dismissState,
        enableDismissFromStartToEnd = swipeEnabled,   // ← condizionato
        enableDismissFromEndToStart = swipeEnabled,   // ← condizionato
        backgroundContent = { /* il tuo sfondo */ },
        content = { /* il contenuto della riga */ }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(bgColor),
            contentAlignment = when {
                swipingRight -> Alignment.CenterStart
                swipingLeft -> Alignment.CenterEnd
                else -> Alignment.Center
            }
        ) {
            if (swipingRight && iconAlpha > 0f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .scale(iconScale)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.endProgressBar.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Preferito",
                            tint = colors.endProgressBar.copy(alpha = iconAlpha),
                        )
                    }

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = favoriteLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.endProgressBar.copy(alpha = iconAlpha),
                        fontSize = 9.sp
                    )
                }
            }

            if (swipingLeft && iconAlpha > 0f) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .scale(iconScale)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.startProgessBar.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Aggiungi in coda",
                            tint = colors.startProgessBar.copy(alpha = iconAlpha),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = "In coda",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.startProgessBar.copy(alpha = iconAlpha),
                        fontSize = 9.sp
                    )
                }
            }

            LaunchedEffect(showConfirmation) {
                if (showConfirmation) {
                    kotlinx.coroutines.delay(1200)
                    showConfirmation = false
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isResetting = false
                            offsetX = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            if (abs(offsetX) >= swipeSlopPx || abs(dragAmount) >= swipeSlopPx) {
                                offsetX += dragAmount
                            }
                        },
                        onDragEnd = {
                            when {
                                offsetX < -confirmThresholdPx -> {
                                    onAddToQueue()
                                    showConfirmation = true
                                    isResetting = true
                                    offsetX = 0f
                                }
                                offsetX > confirmThresholdPx -> {
                                    onFavoriteToggle()
                                    showConfirmation = true
                                    isResetting = true
                                    offsetX = 0f
                                }
                                else -> {
                                    isResetting = true
                                    offsetX = 0f
                                }
                            }
                        },
                        onDragCancel = {
                            isResetting = true
                            offsetX = 0f
                        }
                    )
                }
        ) {
            SongListItem(
                song = song,
                isPlaying = isPlaying,
                isFavorite = isFavorite,
                onClick = onClick,
                onLongClick = onLongClick,
                onFavoriteToggle = onFavoriteToggle,
                onAddToQueue = onAddToQueue,
                favoriteIcon = staticIcon
            )

            AnimatedVisibility(
                visible = showConfirmation,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.bgSidebar.copy(alpha = 0.85f))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = if (swipeEnabled) "Azione confermata" else "Brano nascosto",
                        color = colors.content,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}