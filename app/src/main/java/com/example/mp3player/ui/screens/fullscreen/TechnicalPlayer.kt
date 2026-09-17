package com.example.mp3player.ui.screens.fullscreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.mp3player.player.PlaybackState
import com.example.mp3player.ui.components.CoverArtImage
import com.example.mp3player.ui.components.rememberCircleIndication
import com.example.mp3player.ui.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import java.util.Random
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.mp3player.R

@Composable
fun TechnicalPlayerOverlay(
    playbackState: PlaybackState,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekForward10: () -> Unit,
    onSeekBack10: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenFullscreen: () -> Unit,
) {
    BackHandler(onBack = onClose)

    val colors = AppTheme.colors
    val song = playbackState.currentSong

    // ─────────────────────────────────────────────────────────
    // State
    // ─────────────────────────────────────────────────────────

    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var screenHeightPx by remember { mutableFloatStateOf(0f) }

    var barWidthPx by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val swipeThreshold = 0.20f

    // ─────────────────────────────────────────────────────────
    // Playback
    // ─────────────────────────────────────────────────────────

    val durationMs = song?.duration ?: 0L

    val progressFraction =
        if (durationMs > 0L) {
            (
                    playbackState.currentPositionMs.toFloat() /
                            durationMs.toFloat()
                    ).coerceIn(0f, 1f)
        } else {
            0f
        }

    val displayedFraction =
        if (dragging) dragFraction
        else progressFraction

    // ─────────────────────────────────────────────────────────
    // Progress colors
    // ─────────────────────────────────────────────────────────

    val progressBrush = Brush.horizontalGradient(
        colors = listOf(
            colors.startProgessBar,
            colors.endProgressBar
        )
    )

    // ─────────────────────────────────────────────────────────
    // Animated background
    // ─────────────────────────────────────────────────────────

    AuroraBackground(
        colors = colors,
        isPlaying = playbackState.isPlaying,
        isDarkTheme = isDarkTheme
    )

    // ─────────────────────────────────────────────────────────
    // Main container
    // ─────────────────────────────────────────────────────────

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                screenHeightPx = it.height.toFloat()
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragOffsetY = 0f },
                    onVerticalDrag = { change, amt -> change.consume(); dragOffsetY += amt },
                    onDragCancel = { dragOffsetY = 0f },
                    onDragEnd = {
                        val threshold = screenHeightPx * 0.15f
                        if (screenHeightPx > 0f) {
                            if (dragOffsetY < -threshold) onOpenFullscreen()
                            else if (dragOffsetY > threshold) onClose()
                        }
                        dragOffsetY = 0f
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            PlayerTopBar(
                isDarkTheme = isDarkTheme,
                colors = colors,
                onToggleTheme = onToggleTheme,
                onClose = onClose
            )

            Spacer(modifier = Modifier.height(62.dp))

            // BLOCCO CENTRALE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AlbumArtWithGlow(
                    albumArtUri = song?.albumArtUri,
                    colors = colors
                )

                Spacer(modifier = Modifier.height(32.dp))

                SongInformation(
                    title = song?.title ?: "Nessun brano",
                    artist = song?.artist,
                    album = song?.album,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProgressSection(
                playbackState = playbackState,
                durationMs = durationMs,
                displayedFraction = displayedFraction,
                dragging = dragging,
                dragFraction = dragFraction,
                barWidthPx = barWidthPx,
                progressBrush = progressBrush,
                colors = colors,
                onBarWidthChanged = {
                    barWidthPx = it
                },
                onDragStarted = { fraction ->
                    dragging = true
                    dragFraction = fraction
                },
                onDragChanged = { fraction ->
                    dragFraction = fraction
                },
                onDragFinished = {
                    dragging = false

                    if (durationMs > 0L) {
                        onSeek(
                            (dragFraction * durationMs).toLong()
                        )
                    }
                },
                onDragCancelled = {
                    dragging = false
                },
                onSeek = onSeek
            )

            Spacer(modifier = Modifier.height(16.dp))

            PlayerControls(
                playbackState = playbackState,
                colors = colors,
                onShuffleToggle = onShuffleToggle,
                onSkipPrevious = onSkipPrevious,
                onSeekBack10 = onSeekBack10,
                onPlayPause = onPlayPause,
                onSeekForward10 = onSeekForward10,
                onSkipNext = onSkipNext,
                onRepeatToggle = onRepeatToggle
            )
        }
    }
}



// ============================================================================
// AURORA BACKGROUND
// ============================================================================

@Composable
private fun AuroraBackground(
    colors: com.example.mp3player.ui.theme.AppColors,
    isPlaying: Boolean,
    isDarkTheme: Boolean
) {
    val particles = remember {
        val random = Random(0xCAFE_BABE)

        List(55) {
            AuroraParticle(
                x = random.nextFloat(),
                y = random.nextFloat(),
                speed = 0.025f + random.nextFloat() * 0.055f,
                size = 1.2f + random.nextFloat() * 2.2f,
                alpha = 0.12f + random.nextFloat() * 0.30f,
                phase = random.nextFloat() * (2f * PI.toFloat())
            )
        }
    }

    val transition = rememberInfiniteTransition(
        label = "auroraTransition"
    )

    val rawTime by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 18_000,
                easing = LinearEasing
            )
        ),
        label = "auroraTime"
    )

    val rawPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 5_500,
                easing = FastOutSlowInEasing
            )
        ),
        label = "auroraPulse"
    )

    /*
     * Quando la musica è in pausa:
     * - l'animazione continua
     * - ma si riduce drasticamente
     *
     * Questo evita che lo sfondo sembri completamente "morto",
     * mantenendo però una chiara differenza tra play e pause.
     */
    val animationPower = if (isPlaying) 1f else 0.22f

    val time = rawTime * animationPower

    val wave1 = sin(
        time * 2f * PI.toFloat()
    )

    val wave2 = cos(
        time * 2f * PI.toFloat()
    )

    val wave3 = sin(
        time * 4f * PI.toFloat()
    )

    val themeBoost = if (isDarkTheme) 1f else 2.8f
    val glowStrength =
        (0.75f + rawPulse * 0.25f) *
                (if (isPlaying) 1f else 0.75f) *
                themeBoost

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMainViews)
    ) {

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // -------------------------------------------------
            // Base gradient
            // -------------------------------------------------

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.bgFullscreen,
                        colors.bgMainViews.copy(alpha = 0.55f),
                        colors.bgPlayerBar.copy(alpha = 0.95f)
                    )
                )
            )

            // -------------------------------------------------
            // Aurora — detail
            // -------------------------------------------------

            val detailCenter = Offset(
                x = width * (0.18f + wave1 * 0.10f),
                y = height * (0.30f + wave2 * 0.08f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.detail.copy(
                            alpha = 0.17f * glowStrength
                        ),
                        colors.detail.copy(alpha = 0.055f),
                        Color.Transparent
                    ),
                    center = detailCenter,
                    radius = width * 0.62f
                ),
                radius = width * 0.62f,
                center = detailCenter
            )

            // -------------------------------------------------
            // Aurora — interactive
            // -------------------------------------------------

            val interactiveCenter = Offset(
                x = width * (0.82f - wave2 * 0.12f),
                y = height * (0.52f + wave3 * 0.10f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.interactive.copy(
                            alpha = 0.13f * glowStrength
                        ),
                        colors.interactive.copy(alpha = 0.045f),
                        Color.Transparent
                    ),
                    center = interactiveCenter,
                    radius = width * 0.58f
                ),
                radius = width * 0.58f,
                center = interactiveCenter
            )

            // -------------------------------------------------
            // Aurora — selected border
            // -------------------------------------------------

            val selectedCenter = Offset(
                x = width * (0.50f + wave3 * 0.16f),
                y = height * (0.82f + wave1 * 0.06f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.selectedBorder.copy(
                            alpha = 0.09f * glowStrength
                        ),
                        colors.selectedBorder.copy(alpha = 0.025f),
                        Color.Transparent
                    ),
                    center = selectedCenter,
                    radius = width * 0.52f
                ),
                radius = width * 0.52f,
                center = selectedCenter
            )

            // -------------------------------------------------
            // Center breathing glow
            // -------------------------------------------------

            val centerPulse =
                0.5f + 0.5f * sin(
                    time * 2f * PI.toFloat()
                )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.detail.copy(
                            alpha = (
                                    0.055f +
                                            centerPulse * 0.025f
                                    ) * glowStrength
                        ),
                        Color.Transparent
                    ),
                    center = Offset(
                        width * 0.5f,
                        height * 0.42f
                    ),
                    radius = width * 0.40f
                ),
                radius = width * 0.40f,
                center = Offset(
                    width * 0.5f,
                    height * 0.42f
                )
            )

            // -------------------------------------------------
            // Floating particles
            // -------------------------------------------------

            particles.forEach { particle ->

                val yRaw =
                    particle.y -
                            particle.speed * time * 3f

                val y =
                    ((yRaw % 1f) + 1f) % 1f

                val drift =
                    sin(
                        time * 2f * PI.toFloat() +
                                particle.phase
                    ) * 0.012f

                val particlePulse =
                    0.5f + 0.5f * sin(
                        time * 4f * PI.toFloat() +
                                particle.phase
                    )

                val alpha =
                    particle.alpha *
                            (
                                    0.25f +
                                            particlePulse * 0.55f
                                    )

                drawCircle(
                    color = colors.content.copy(
                        alpha = alpha
                    ),
                    radius = particle.size.dp.toPx(),
                    center = Offset(
                        (particle.x + drift) * width,
                        y * height
                    )
                )
            }
        }

        // -----------------------------------------------------
        // Vignette
        // -----------------------------------------------------

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            colors.bgFullscreen.copy(alpha = 0.16f),
                            colors.bgFullscreen.copy(alpha = 0.48f)
                        ),
                        radius = 1200f
                    )
                )
        )
    }
}


private data class AuroraParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float,
    val phase: Float
)


// ============================================================================
// TOP BAR
// ============================================================================

@Composable
private fun PlayerTopBar(
    isDarkTheme: Boolean,
    colors: com.example.mp3player.ui.theme.AppColors,
    onToggleTheme: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = onToggleTheme
        ) {
            Icon(
                imageVector = if (isDarkTheme) {
                    Icons.Default.WbSunny
                } else {
                    Icons.Default.NightsStay
                },
                contentDescription = if (isDarkTheme) {
                    "Tema chiaro"
                } else {
                    "Tema scuro"
                },
                tint = colors.content.copy(alpha = 0.6f)
            )
        }

        IconButton(
            onClick = onClose
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Chiudi",
                tint = colors.content.copy(alpha = 0.6f)
            )
        }
    }
}


// ============================================================================
// ALBUM ART
// ============================================================================

@Composable
private fun AlbumArtWithGlow(
    albumArtUri: String?,
    colors: com.example.mp3player.ui.theme.AppColors
) {
    Box(
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier.size(320.dp)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.interactive.copy(alpha = 0.11f),
                        colors.detail.copy(alpha = 0.05f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.5f,
                center = center
            )
        }

        CoverArtImage(
            albumArtUri = albumArtUri,
            size = 340.dp,
            cornerRadius = 16.dp
        )
    }
}


// ============================================================================
// SONG INFORMATION
// ============================================================================

@Composable
private fun SongInformation(
    title: String,
    artist: String?,
    album: String?,
    colors: com.example.mp3player.ui.theme.AppColors
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = colors.content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    if (!artist.isNullOrBlank()) {
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.content.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (!album.isNullOrBlank()) {
        Text(
            text = album,
            style = MaterialTheme.typography.bodySmall,
            color = colors.content.copy(alpha = 0.4f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


// ============================================================================
// PROGRESS
// ============================================================================

@Composable
private fun ProgressSection(
    playbackState: PlaybackState,
    durationMs: Long,
    displayedFraction: Float,
    dragging: Boolean,
    dragFraction: Float,
    barWidthPx: Float,
    progressBrush: Brush,
    colors: com.example.mp3player.ui.theme.AppColors,
    onBarWidthChanged: (Float) -> Unit,
    onDragStarted: (Float) -> Unit,
    onDragChanged: (Float) -> Unit,
    onDragFinished: () -> Unit,
    onDragCancelled: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .onSizeChanged {
                onBarWidthChanged(
                    it.width.toFloat()
                )
            }
            .pointerInput(durationMs, barWidthPx) {
                detectTapGestures { offset ->

                    if (
                        barWidthPx > 0f &&
                        durationMs > 0L
                    ) {
                        val fraction =
                            (
                                    offset.x /
                                            barWidthPx
                                    ).coerceIn(0f, 1f)

                        onSeek(
                            (
                                    fraction *
                                            durationMs
                                    ).toLong()
                        )
                    }
                }
            }
            .pointerInput(durationMs, barWidthPx) {
                detectHorizontalDragGestures(

                    onDragStart = { offset ->

                        val fraction =
                            if (barWidthPx > 0f) {
                                (
                                        offset.x /
                                                barWidthPx
                                        ).coerceIn(0f, 1f)
                            } else {
                                displayedFraction
                            }

                        onDragStarted(fraction)
                    },

                    onHorizontalDrag = { change, _ ->

                        change.consume()

                        if (barWidthPx > 0f) {
                            onDragChanged(
                                (
                                        change.position.x /
                                                barWidthPx
                                        ).coerceIn(0f, 1f)
                            )
                        }
                    },

                    onDragEnd = {
                        onDragFinished()
                    },

                    onDragCancel = {
                        onDragCancelled()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {

        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    colors.content.copy(alpha = 0.12f)
                )
        )

        // Progress
        if (displayedFraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(displayedFraction)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(progressBrush)
                    .align(Alignment.CenterStart)
            )
        }

        // Thumb
        if (dragging) {

            val density = LocalDensity.current

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .offset {
                        IntOffset(
                            x = (
                                    dragFraction * barWidthPx -
                                            with(density) {
                                                7.dp.toPx()
                                            }
                                    ).toInt(),
                            y = 0
                        )
                    }
                    .clip(CircleShape)
                    .background(colors.interactive)
                    .align(Alignment.CenterStart)
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = formatMillis(
                playbackState.currentPositionMs
            ),
            style = MaterialTheme.typography.labelSmall,
            color = colors.content.copy(alpha = 0.5f)
        )

        Text(
            text = formatMillis(durationMs),
            style = MaterialTheme.typography.labelSmall,
            color = colors.content.copy(alpha = 0.5f)
        )
    }
}


// ============================================================================
// PLAYER CONTROLS
// ============================================================================

@Composable
private fun PlayerControls(
    playbackState: PlaybackState,
    colors: com.example.mp3player.ui.theme.AppColors,
    onShuffleToggle: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekBack10: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekForward10: () -> Unit,
    onSkipNext: () -> Unit,
    onRepeatToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // -----------------------------------------------------
        // Shuffle
        // -----------------------------------------------------

        PlayerToggleButton(
            enabled = playbackState.isShuffleEnabled,
            icon = painterResource(id = R.drawable.shuffle),
            contentDescription = "Shuffle",
            colors = colors,
            tint = colors.content,
            onClick = onShuffleToggle
        )

        // -----------------------------------------------------
        // Previous
        // -----------------------------------------------------

        IconButton(
            onClick = onSkipPrevious
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Precedente",
                tint = colors.content
            )
        }

        // -----------------------------------------------------
        // -10
        // -----------------------------------------------------

        IconButton(
            onClick = onSeekBack10
        ) {
            Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = "-10s",
                tint = colors.content
            )
        }

        // -----------------------------------------------------
        // Play / Pause
        // -----------------------------------------------------

        PlayerMainButton(
            isPlaying = playbackState.isPlaying,
            colors = colors,
            onClick = onPlayPause
        )

        // -----------------------------------------------------
        // +10
        // -----------------------------------------------------

        IconButton(
            onClick = onSeekForward10
        ) {
            Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = "+10s",
                tint = colors.content
            )
        }

        // -----------------------------------------------------
        // Next
        // -----------------------------------------------------

        IconButton(
            onClick = onSkipNext
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Successiva",
                tint = colors.content
            )
        }

        // -----------------------------------------------------
        // Repeat
        // -----------------------------------------------------

        PlayerToggleButton(
            enabled = playbackState.isRepeatEnabled,
            icon = painterResource(id = R.drawable.repeat),
            contentDescription = "Repeat",
            colors = colors,
            tint = colors.content,
            onClick = onRepeatToggle
        )
    }
}


// ============================================================================
// TOGGLE BUTTON
// ============================================================================
@Composable
private fun PlayerToggleButton(
    enabled: Boolean,
    icon: Painter, // Modificato da ImageVector a Painter
    contentDescription: String,
    colors: com.example.mp3player.ui.theme.AppColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = colors.content
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(
                if (enabled) colors.selectedBg else Color.Transparent
            )
            .then(
                if (enabled) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = colors.selectedBorder,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberCircleIndication(
                    color = colors.interactive
                ),
                onClick = onClick
            )
            .padding(10.dp)
    ) {
        Icon(
            painter = icon, // Usa il Painter direttamente
            contentDescription = contentDescription,
            tint = colors.content,
            modifier = Modifier.size(22.dp)
        )
    }
}


// ============================================================================
// MAIN PLAY BUTTON
// ============================================================================

@Composable
private fun PlayerMainButton(
    isPlaying: Boolean,
    colors: com.example.mp3player.ui.theme.AppColors,
    onClick: () -> Unit
) {
    val interactionSource =
        remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(colors.selectedBg)
            .border(
                width = 1.5.dp,
                color = colors.selectedBorder,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberCircleIndication(
                    color = colors.interactive
                ),
                onClick = onClick
            )
    ) {

        Icon(
            imageVector =
                if (isPlaying) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
            contentDescription =
                if (isPlaying) {
                    "Pausa"
                } else {
                    "Play"
                },
            tint = colors.content,
            modifier = Modifier.size(32.dp)
        )
    }
}


// ============================================================================
// TIME FORMAT
// ============================================================================

private fun formatMillis(
    ms: Long
): String {
    val seconds =
        (ms / 1000).coerceAtLeast(0)

    return "%d:%02d".format(
        seconds / 60,
        seconds % 60
    )
}