package com.example.mp3player.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mp3player.ui.theme.AppTheme
import kotlinx.coroutines.delay
import androidx.compose.ui.res.painterResource
import com.example.mp3player.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerBar(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    songTitle: String = "Nessun brano",
    artistName: String = "",
    albumArtUri: String? = null,
    progressFraction: Float = 0f,
    currentTime: String = "0:00",
    totalTime: String = "0:00",
    durationMs: Long = 0L,
    shuffleEnabled: Boolean = false,
    repeatEnabled: Boolean = false,
    onShuffleToggle: () -> Unit = {},
    onRepeatToggle: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onSwipeNext: () -> Unit = {},
    onSwipePrevious: () -> Unit = {},
    onTechnicalOpen: () -> Unit = {},
    onFullscreenOpen: () -> Unit = {},
    onOpenFullscreen: () -> Unit,
    onLongPress: () -> Unit = {},
) {
    val colors = AppTheme.colors
    val playerShape = RoundedCornerShape(20.dp)

    var barWidthPx by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(progressFraction) }

    var swipeX by remember { mutableFloatStateOf(0f) }
    var swipeY by remember { mutableFloatStateOf(0f) }

    var showControls by remember { mutableStateOf(false) }

    // Mostra Shuffle/Repeat per 3 secondi.
    // Il play/pause rimane sempre visibile.
    fun revealControls() {
        showControls = true
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    val controlsAlpha by animateFloatAsState(
        targetValue = if (showControls) 1f else 0f,
        animationSpec = tween(200),
        label = "controlsAlpha"
    )

    val displayedFraction =
        if (dragging) dragFraction else progressFraction

    val contentColor = colors.content
    val secondaryColor = contentColor.copy(alpha = 0.6f)

    val progressBrush = Brush.horizontalGradient(
        colors = listOf(
//            colors.detail,
//            colors.interactive
            colors.startProgessBar,
            colors.endProgressBar
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        swipeX = 0f
                    },
                    onHorizontalDrag = { change, amount ->
                        change.consume()
                        swipeX += amount
                    },
                    onDragEnd = {
                        when {
                            swipeX < -80f -> onSwipeNext()
                            swipeX > 80f -> onSwipePrevious()
                        }
                        swipeX = 0f
                    },
                    onDragCancel = {
                        swipeX = 0f
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, amount ->
                        if (amount < 0f) {
                            swipeY += -amount
                        }
                    },
                    onDragEnd = {
                        if (swipeY >= size.height * 0.4f) {
                            onOpenFullscreen()
                        }
                        swipeY = 0f
                    },
                    onDragCancel = {
                        swipeY = 0f
                    }
                )
            }
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = playerShape,
                    clip = false,
                    ambientColor = colors.shadow_color,
                    spotColor = colors.shadow_color
                ),
            shape = playerShape,
            color = colors.bgPlayerBar,
            tonalElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {

                // ── Contenuto principale ─────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 10.dp,
                            bottom = 14.dp
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    onTechnicalOpen()
                                },
                                onDoubleTap = {
                                    onFullscreenOpen()
                                },
                                onLongPress = {
                                    onLongPress()
                                }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // ── Copertina ─────────────────────────────────────
                        CoverArtImage(
                            albumArtUri = albumArtUri,
                            size = 48.dp,
                            cornerRadius = 6.dp
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        // ── Titolo / artista ──────────────────────────────
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = songTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee()
                            )

                            if (artistName.isNotBlank()) {
                                Text(
                                    text = artistName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = secondaryColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // ── CONTROLLI ─────────────────────────────────────
                        //
                        // IMPORTANTE:
                        // Shuffle e Repeat sono SEMPRE presenti nel layout.
                        // Quando nascosti hanno alpha = 0, quindi non occupano
                        // visivamente spazio ma mantengono fisso il Play/Pause.
                        //
                        Row(
                            modifier = Modifier.width(144.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            // Shuffle
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .alpha(controlsAlpha),
                                contentAlignment = Alignment.Center
                            ) {
                                ToggleButton(
                                    enabled = shuffleEnabled,
                                    icon = painterResource(id = R.drawable.shuffle),
                                    description = "Riproduzione casuale",
                                    color = contentColor,
                                    selectedBg = colors.selectedBg,
                                    selectedBorder = colors.selectedBorder,
                                    onClick = {
                                        revealControls()
                                        onShuffleToggle()
                                    }
                                )
                            }

                            // Play / Pause
                            IconButton(
                                onClick = {
                                    revealControls()
                                    onPlayPause()
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) {
                                        Icons.Default.Pause
                                    } else {
                                        Icons.Default.PlayArrow
                                    },
                                    contentDescription =
                                        if (isPlaying) "Pausa"
                                        else "Play",
                                    tint = contentColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Repeat
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .alpha(controlsAlpha),
                                contentAlignment = Alignment.Center
                            ) {
                                ToggleButton(
                                    enabled = repeatEnabled,
                                    icon = painterResource(id = R.drawable.repeat),
                                    description = "Ripeti",
                                    color = contentColor,
                                    selectedBg = colors.selectedBg,
                                    selectedBorder = colors.selectedBorder,
                                    onClick = {
                                        revealControls()
                                        onRepeatToggle()
                                    }
                                )
                            }
                        }
                    }
                }

                // ── Progress bar ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .align(Alignment.BottomCenter)
                        .onSizeChanged {
                            barWidthPx = it.width.toFloat()
                        }
                        .pointerInput(durationMs) {
                            detectTapGestures { offset ->
                                if (
                                    barWidthPx > 0f &&
                                    durationMs > 0L
                                ) {
                                    val fraction =
                                        (offset.x / barWidthPx)
                                            .coerceIn(0f, 1f)

                                    onSeek(
                                        (fraction * durationMs).toLong()
                                    )
                                }
                            }
                        }
                        .pointerInput(durationMs) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    dragging = true

                                    dragFraction =
                                        if (barWidthPx > 0f) {
                                            (offset.x / barWidthPx)
                                                .coerceIn(0f, 1f)
                                        } else {
                                            progressFraction
                                        }
                                },

                                onHorizontalDrag = { change, _ ->
                                    change.consume()

                                    if (barWidthPx > 0f) {
                                        dragFraction =
                                            (change.position.x / barWidthPx)
                                                .coerceIn(0f, 1f)
                                    }
                                },

                                onDragEnd = {
                                    dragging = false

                                    if (durationMs > 0L) {
                                        onSeek(
                                            (
                                                    dragFraction * durationMs
                                                    ).toLong()
                                        )
                                    }
                                },

                                onDragCancel = {
                                    dragging = false
                                }
                            )
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {

                    // Traccia praticamente invisibile.
                    // Non crea più una evidente riga grigia.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                contentColor.copy(alpha = 0.06f)
                            )
                    )

                    // Progresso
                    if (displayedFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(displayedFraction)
                                .height(2.dp)
                                .align(Alignment.BottomStart)
                                .background(progressBrush)
                        )
                    }

                    // Thumb
                    if (dragging) {
                        val density = LocalDensity.current
                        val thumbSize = 12.dp
                        val thumbRadiusPx =
                            with(density) {
                                thumbSize.toPx() / 2f
                            }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(thumbSize)
                                    .align(Alignment.BottomStart)
                                    .offset(
                                        x = with(density) {
                                            (
                                                    displayedFraction *
                                                            barWidthPx -
                                                            thumbRadiusPx
                                                    ).toDp()
                                        }
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        colors.endProgressBar
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleButton(
    enabled: Boolean,
    icon: Painter,
    description: String,
    color: Color,
    selectedBg: Color,
    selectedBorder: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                if (enabled) {
                    selectedBg
                } else {
                    Color.Transparent
                }
            )
            .then(
                if (enabled) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = selectedBorder,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = icon, // Corretto l'errore di sintassi in minuscolo
            contentDescription = description,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
    }
}