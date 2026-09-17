package com.example.mp3player.ui.screens.fullscreen

import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mp3player.player.PlaybackState
import com.example.mp3player.ui.components.CoverArtImage
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import com.example.mp3player.ui.theme.AppTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.example.mp3player.ui.components.rememberCircleIndication
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mp3player.ui.theme.AppColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import com.example.mp3player.R
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
@Composable
fun FullscreenPlayerOverlay(
    playbackState: PlaybackState,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onSeekRelative: (millis: Long) -> Unit,
    onSeek: (Long) -> Unit,
    isDarkTheme: Boolean,
    onTechnicalOpen: () -> Unit = {},
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val colors = AppTheme.colors
    val song = playbackState.currentSong
    val durationMs = song?.duration ?: 0L
    val progressFraction = if (durationMs > 0L)
        (playbackState.currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    else 0f

    // ── Estrazione colore dominante via Palette ────────────────
    val context = LocalContext.current
    var paletteColors by remember { mutableStateOf<List<Color>>(emptyList()) }

    LaunchedEffect(song?.albumArtUri) {
        paletteColors = emptyList()
        val uri = song?.albumArtUri ?: return@LaunchedEffect
        try {
            val loader = ImageLoader(context)
            val req = ImageRequest.Builder(context)
                .data(uri).allowHardware(false).build()
            val result = loader.execute(req)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    ?: return@LaunchedEffect
                Palette.from(bitmap).generate { palette ->
                    if (palette == null) return@generate
                    // Raccoglie tutti i swatch disponibili nell'ordine di "carattere"
                    val swatches = listOfNotNull(
                        palette.vibrantSwatch,
                        palette.lightVibrantSwatch,
                        palette.darkVibrantSwatch,
                        palette.mutedSwatch,
                        palette.lightMutedSwatch,
                        palette.darkMutedSwatch,
                        palette.dominantSwatch
                    ).map { Color(it.rgb) }.distinct()
                    paletteColors = swatches
                }
            }
        } catch (_: Exception) {}
    }
    // Solo play e progress bar usano il colore copertina
    val accentColor = paletteColors.firstOrNull() ?: colors.interactive

    // Stato skip
    var skipVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var hideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    var coverOffsetX by remember { mutableFloatStateOf(0f) }

    val animatedCoverOffsetX by animateFloatAsState(
        targetValue = coverOffsetX,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 180f
        ),
        label = "coverSwipe"
    )

    fun showControls() {
        skipVisible = true
        hideJob?.cancel()
        hideJob = scope.launch {
            delay(3_000L)
            skipVisible = false
        }
    }

    fun hideControls() {
        hideJob?.cancel()
        skipVisible = false
    }


    fun onPlayPauseTap() {
        onPlayPause()
    }

    fun onSkipTap(action: () -> Unit) {
        action()
        hideJob?.cancel()
        hideJob = scope.launch { delay(5_000L); skipVisible = false }
    }

    // ── Layout principale ──────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            // Sfondo fisso dal tema, mai dal colore copertina
            .background(colors.bgFullscreen)
            .pointerInput(Unit) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onVerticalDrag = { _, amt -> totalDrag += amt },
                    onDragCancel = { totalDrag = 0f },
                    onDragEnd = {
                        val threshold = size.height * 0.15f
                        if (totalDrag >= threshold) onClose()
                        else if (totalDrag <= -threshold) onTechnicalOpen()
                        totalDrag = 0f
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (skipVisible) {
                            hideControls()
                        } else {
                            showControls()
                        }
                    },
                    onDoubleTap = { offset ->
                        val isRightSide = offset.x > size.width / 2
                        onSeekRelative(if (isRightSide) 10_000L else -10_000L)
                    }
                )
            }.pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()

                        coverOffsetX += dragAmount
                    },
                    onDragEnd = {
                        val threshold = 150f

                        when {
                            coverOffsetX < -threshold -> {
                                onSkipNext()
                            }

                            coverOffsetX > threshold -> {
                                onSkipPrevious()
                            }
                        }

                        coverOffsetX = 0f
                    },
                    onDragCancel = {
                        coverOffsetX = 0f
                    }
                )
            }
    ) {
        // Sfondo a schegge accentuato con il colore base del tema (non copertina)
        GlassShardBackground(
            color = if (isDarkTheme)
                colors.content.copy(alpha = 0.06f)   // schegge bianche leggere sul nero
            else
                colors.content.copy(alpha = 0.08f),  // schegge scure leggere sul bianco
            modifier = Modifier.fillMaxSize()
        )

//        CoverSideGlows(
//            colorA = colors.detail,
//            colorB = colors.interactive,
//            modifier = Modifier.fillMaxSize()
//        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = if (isDarkTheme) "Tema chiaro" else "Tema scuro",
                        tint = colors.content.copy(alpha = 0.7f)
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Chiudi",
                        tint = colors.content.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(124.dp))

            // ── Copertina ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = animatedCoverOffsetX
                        rotationZ = animatedCoverOffsetX / 20f
                    }
            ) {
                CoverArtImage(
                    albumArtUri = song?.albumArtUri,
                    size = 240.dp,
                    cornerRadius = 20.dp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ── Titolo ─────────────────────────────────────────────
            Text(
                text = song?.title ?: "Nessun brano",
                style = MaterialTheme.typography.titleLarge,
                color = colors.content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Artista ────────────────────────────────────────────
            if (!song?.artist.isNullOrBlank()) {
                Text(
                    text = song!!.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.content.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Album ──────────────────────────────────────────────
            if (!song?.album.isNullOrBlank()) {
                Text(
                    text = song!!.album,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.content.copy(alpha = 0.4f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // ── Progress bar ───────────────────────────────────────
            ModernProgressBar(
                progress = progressFraction,
                isPlaying = playbackState.isPlaying,
                paletteColors = paletteColors,
                fallbackColor = colors.interactive,
                trackColor = colors.content.copy(alpha = 0.12f),
                onSeek = { fraction ->
                    onSeek((durationMs * fraction).toLong())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(92.dp))

            // ── Controlli ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = skipVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        FullscreenToggleButton(
                            enabled = playbackState.isShuffleEnabled,
                            icon = painterResource(id = R.drawable.shuffle),
                            contentDescription = "Shuffle",
                            colors = colors,
                            accentColor = accentColor,
                            onClick = {
                                onShuffleToggle()
                                if (!playbackState.isShuffleEnabled && playbackState.isRepeatEnabled) {
                                    onRepeatToggle()
                                }
                                showControls()
                            }
                        )

                        IconButton(
                            onClick = { onSkipTap(onSkipPrevious) }
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Precedente",
                                modifier = Modifier.size(36.dp),
                                tint = colors.content.copy(alpha = 0.8f)
                            )
                        }

                        FilledIconButton(
                            onClick = { onPlayPauseTap() },
                            modifier = Modifier.size(64.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = accentColor,
                                contentColor = if (accentColor.luminance() > 0.5f)
                                    Color.Black else Color.White
                            )
                        ) {
                            Icon(
                                imageVector = if (playbackState.isPlaying)
                                    Icons.Default.Pause
                                else
                                    Icons.Default.PlayArrow,
                                contentDescription =
                                    if (playbackState.isPlaying) "Pausa" else "Play",
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(
                            onClick = { onSkipTap(onSkipNext) }
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Successiva",
                                modifier = Modifier.size(36.dp),
                                tint = colors.content.copy(alpha = 0.8f)
                            )
                        }

                        FullscreenToggleButton(
                            enabled = playbackState.isRepeatEnabled,
                            icon = painterResource(id = R.drawable.repeat),
                            contentDescription = "Repeat",
                            colors = colors,
                            accentColor = accentColor,
                            onClick = {
                                onRepeatToggle()
                                if (!playbackState.isRepeatEnabled && playbackState.isShuffleEnabled) {
                                    onShuffleToggle()
                                }
                                showControls()
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Progress bar moderna con barre verticali animate
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ModernProgressBar(
    progress: Float,
    isPlaying: Boolean,
    paletteColors: List<Color>,
    fallbackColor: Color,
    trackColor: Color,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = remember(paletteColors, fallbackColor) {
        if (paletteColors.size >= 2) paletteColors else List(6) { fallbackColor }
    }

    var animationTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { animationTime = it }
        }
    }

    val amplitude by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.03f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 80f),
        label = "amplitude"
    )

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = { offset ->
                    val fraction = (offset.x / size.width)
                        .coerceIn(0f, 1f)

                    onSeek(fraction)
                },
                onHorizontalDrag = { change, _ ->
                    change.consume()

                    val fraction = (change.position.x / size.width)
                        .coerceIn(0f, 1f)

                    onSeek(fraction)
                }
            )
        }
    ) {
        val w = size.width
        val h = size.height
        val barCount = 42
        val totalSlot = w / barCount
        val barWidth = totalSlot * 0.55f
        val gap = totalSlot - barWidth
        val splitX = w * progress.coerceIn(0f, 1f)
        val maxBarH = h * 0.9f
        val minBarH = h * 0.06f

        val seconds = animationTime / 1_000_000_000.0
        val phase = seconds * (2.0 * PI / 2.2)
        val phase2 = seconds * (2.0 * PI / 3.7)
        val colorOffset = seconds * (palette.size.toDouble() / 4.0)

        for (i in 0 until barCount) {
            val x = i * totalSlot + gap / 2f
            val center = x + barWidth / 2f
            val t = i.toDouble() / barCount

            val wave =
                0.45 * sin(2.0 * PI * 1.5 * t + phase) +
                        0.30 * sin(2.0 * PI * 3.7 * t + phase2) +
                        0.25 * sin(2.0 * PI * 7.1 * t + phase * 0.6 + phase2 * 0.4)

            val normalized = ((wave + 1.0) / 2.0).coerceIn(0.0, 1.0).toFloat()
            val barHeight = minBarH + amplitude * (maxBarH - minBarH) * normalized
            val top = (h - barHeight) / 2f

            if (center <= splitX) {
                val rawIndex = (t * palette.size + colorOffset) % palette.size.toDouble()
                val indexA = rawIndex.toInt() % palette.size
                val indexB = (indexA + 1) % palette.size
                val fraction = (rawIndex - rawIndex.toInt()).toFloat()
                val barColor = lerp(palette[indexA], palette[indexB], fraction)

                drawRoundRect(
                    color = barColor.copy(alpha = 0.18f),
                    topLeft = Offset(x - 1.5f, top + barHeight * 0.15f),
                    size = Size(barWidth + 3f, barHeight * 0.7f),
                    cornerRadius = CornerRadius(barWidth / 2f)
                )

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f)
                )
            } else {
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f)
                )
            }
        }

        if (progress > 0.01f && progress < 0.99f) {
            val ix = splitX
            val iy = h / 2f
            val r = 5.dp.toPx()
            val tipColor = palette.firstOrNull() ?: fallbackColor

            drawCircle(
                color = tipColor.copy(alpha = 0.25f),
                radius = r * 2.2f,
                center = Offset(ix, iy)
            )

            val path = Path().apply {
                moveTo(ix, iy - r)
                lineTo(ix + r, iy)
                lineTo(ix, iy + r)
                lineTo(ix - r, iy)
                close()
            }

            drawPath(path = path, color = tipColor)
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Sfondo a schegge — ora riceve direttamente il colore finale già calcolato
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GlassShardBackground(
    color: Color,
    modifier: Modifier = Modifier
) {
    val shards = remember(color) { buildShards(color) }
    Canvas(modifier = modifier) {
        shards.forEach { shard ->
            drawPath(path = shard.path(size.width, size.height), color = shard.color)
        }
    }
}

@Composable
private fun CoverSideGlows(
    colorA: Color,
    colorB: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "sideGlows")

    val alphaLeft by transition.animateFloat(
        initialValue = 0.06f, targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "gL"
    )
    val alphaRight by transition.animateFloat(
        initialValue = 0.18f, targetValue = 0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "gR"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val radius = w * 0.28f   // più stretto rispetto agli angoli
        val centerY = h * 0.42f  // allineato alla copertina (che è a ~Spacer 124dp + 240dp/2)

        // Lato sinistro → colorA
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colorA.copy(alpha = alphaLeft), Color.Transparent),
                center = Offset(0f, centerY),
                radius = radius
            ),
            radius = radius,
            center = Offset(0f, centerY)
        )
        // Lato destro → colorB
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colorB.copy(alpha = alphaRight), Color.Transparent),
                center = Offset(w, centerY),
                radius = radius
            ),
            radius = radius,
            center = Offset(w, centerY)
        )
    }
}
private data class ShardDef(
    val x0: Float, val y0: Float,
    val x1: Float, val y1: Float,
    val x2: Float, val y2: Float,
    val color: Color
)

private fun ShardDef.path(w: Float, h: Float) = Path().apply {
    moveTo(x0 * w, y0 * h); lineTo(x1 * w, y1 * h); lineTo(x2 * w, y2 * h); close()
}

private fun buildShards(base: Color): List<ShardDef> {
    val cols = 5; val rows = 8
    val rng = java.util.Random(0xDEADBEEF)
    val pts = Array(rows + 1) { r ->
        Array(cols + 1) { c ->
            val jx = if (c in 1 until cols) rng.nextFloat() * 0.15f - 0.075f else 0f
            val jy = if (r in 1 until rows) rng.nextFloat() * 0.10f - 0.05f  else 0f
            Pair(c.toFloat() / cols + jx, r.toFloat() / rows + jy)
        }
    }
    val shards = mutableListOf<ShardDef>()
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val tl = pts[r][c];   val tr = pts[r][c + 1]
            val bl = pts[r+1][c]; val br = pts[r+1][c+1]
            // Alpha più alta rispetto a prima per accentuare le schegge
            val a1 = 0.06f + rng.nextFloat() * 0.10f
            val a2 = 0.06f + rng.nextFloat() * 0.10f
            shards += ShardDef(tl.first,tl.second, tr.first,tr.second, bl.first,bl.second, base.copy(alpha=a1))
            shards += ShardDef(tr.first,tr.second, br.first,br.second, bl.first,bl.second, base.copy(alpha=a2))
        }
    }
    return shards
}

@Composable
private fun FullscreenToggleButton(
    enabled: Boolean,
    icon: Painter, // Accetta solo Painter
    contentDescription: String,
    colors: AppColors,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val tintColor = if (enabled) {
        if (accentColor.luminance() > 0.5f) Color.Black else Color.White
    } else {
        colors.content.copy(alpha = 0.5f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (enabled) accentColor else Color.Transparent)
            .then(
                if (enabled) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = accentColor,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberCircleIndication(color = colors.interactive),
                onClick = onClick
            )
    ) {
        // Disegna direttamente il Painter passato come parametro
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = Modifier.size(20.dp)
        )
    }
}


private fun formatMillis(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}