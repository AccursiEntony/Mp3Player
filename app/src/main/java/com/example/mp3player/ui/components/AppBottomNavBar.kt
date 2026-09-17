package com.example.mp3player.ui.components

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mp3player.ui.navigation.BottomNavItems
import com.example.mp3player.ui.theme.AppTheme
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import com.example.mp3player.ui.navigation.StatisticsNavItem
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
@Composable
fun AppBottomNavBar(
    navController: NavHostController,
    isPlaying: Boolean,
    showStatisticsTab: Boolean = false
) {
    var swipeDeltaX by remember { mutableFloatStateOf(0f) }
    val swipeThresholdPx = with(LocalDensity.current) { 60.dp.toPx() }

    val colors  = AppTheme.colors
    val context = LocalContext.current

    val items = remember(showStatisticsTab) {
        if (showStatisticsTab) BottomNavItems + StatisticsNavItem else BottomNavItems
    }

    val currentRoute  = navController.currentBackStackEntryAsState().value?.destination?.route
    val selectedIndex = remember(currentRoute, items) {
        items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    }

    var lastClickedRoute by remember { mutableStateOf<String?>(null) }
    var clickCount       by remember { mutableIntStateOf(0) }

    // ── Animazione basata sull'indice, non su coordinate assolute ──────────
    // tabWidthPx viene calcolato una sola volta dal layout reale (BoxWithConstraints).
    // L'indicatore si muove di esattamente (selectedIndex * tabWidth), garantito.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bgBottomNavbar)
            .padding(vertical = 10.dp)
            .pointerInput(items.size, selectedIndex) {
                detectHorizontalDragGestures(
                    onDragStart = { swipeDeltaX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        swipeDeltaX += dragAmount
                    },
                    onDragEnd = {
                        when {
                            swipeDeltaX > swipeThresholdPx -> {
                                // dito va a destra → pagina successiva (a destra)
                                val next = (selectedIndex + 1) % items.size
                                navController.navigate(items[next].route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            swipeDeltaX < -swipeThresholdPx -> {
                                // dito va a sinistra → pagina precedente (a sinistra)
                                val prev = (selectedIndex - 1 + items.size) % items.size
                                navController.navigate(items[prev].route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        swipeDeltaX = 0f
                    },
                    onDragCancel = { swipeDeltaX = 0f }
                )
            }
    ) {
        val tabWidthPx  = constraints.maxWidth / items.size.toFloat()
        val indicatorPx = 48.dp
        val density     = LocalDensity.current

        // Target in pixel: centro del tab selezionato, meno metà indicatore
        val targetOffsetX = tabWidthPx * selectedIndex + tabWidthPx / 2f -
                with(density) { indicatorPx.toPx() / 2f }

        val animatedOffsetX by animateFloatAsState(
            targetValue    = targetOffsetX,
            animationSpec  = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,  // leggero bounce
                stiffness    = Spring.StiffnessMedium
            ),
            label = "IndicatorX"
        )

        // INDICATORE
        Box(
            modifier = Modifier
                .size(indicatorPx)
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(colors.selectedBg)
                .border(1.5.dp, colors.selectedBorder, CircleShape)
        )

        // ICONE
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val interactionSource = remember { MutableInteractionSource() }

                val iconScale by animateFloatAsState(
                    targetValue   = if (isSelected) 1.18f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium
                    ),
                    label = "IconScale_$index"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(indicatorPx)
                    ) {
                        if (isSelected && isPlaying) {
                            PulsingRings(
                                color    = colors.selectedBorder,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(indicatorPx)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = rememberCircleIndication(colors.interactive),
                                    onClick = {
                                        if (lastClickedRoute == item.route) clickCount++
                                        else { lastClickedRoute = item.route; clickCount = 1 }

                                        if (clickCount == 7) {
                                            Toast.makeText(context, "Made by Antonio Accursi", Toast.LENGTH_SHORT).show()
                                            clickCount = 0
                                        }

                                        if (!isSelected) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState    = true
                                            }
                                        }
                                    }
                                )
                        ) {
                            Icon(
                                imageVector     = item.icon,
                                contentDescription = item.title,
                                tint            = colors.content,
                                modifier        = Modifier.size(24.dp).scale(iconScale)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun PulsingRings(
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "lavaLamp")

    // Movimento continuo dei blob: usare un singolo progress evita
    // che i vari elementi sembrino fermi o si sincronizzino male.
    val movement by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "lavaLampMovement"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lavaLampPulse"
    )

    Canvas(
        modifier = modifier.graphicsLayer {
            // Applica l'effetto liquido (Gooey) combinando sfocatura e contrasto sull'alfa.
            // Nota: richiede Android 12 (API 31) o superiore.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val blur = android.graphics.RenderEffect.createBlurEffect(
                    30f, 30f, android.graphics.Shader.TileMode.DECAL
                )

                // Matrice che aumenta drasticamente il contrasto del canale Alfa (quarta riga)
                val alphaContrastMatrix = android.graphics.ColorMatrix(
                    floatArrayOf(
                        1f, 0f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f, 0f,
                        0f, 0f, 1f, 0f, 0f,
                        0f, 0f, 0f, 60f, -3000f
                    )
                )
                val matrixFilter = android.graphics.RenderEffect.createColorFilterEffect(
                    android.graphics.ColorMatrixColorFilter(alphaContrastMatrix)
                )

                renderEffect = android.graphics.RenderEffect.createChainEffect(matrixFilter, blur).asComposeRenderEffect()
            }
        }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Ridotta la scala da 48.dp a 64.dp per creare una "zona di sicurezza" (padding)
        // intorno al disegno, impedendo ai bordi sfocati di essere tagliati dal Canvas.
        val scale = size.minDimension / 64.dp.toPx()

        // Rimosso l'alpha personalizzato dai singoli drawCircle (tutti a 1.0f)
        // perché il filtro gooey richiede elementi solidi per fondersi correttamente.

        // Blob centrale pulsante.
        drawCircle(
            color = color,
            radius = 24.dp.toPx() * pulse * scale,
            center = Offset(cx, cy)
        )

        // Blob laterale sinistro.
        drawCircle(
            color = color,
            radius = 16.dp.toPx() * scale,
            center = Offset(
                cx + (-13f + sin(movement) * 8f).dp.toPx() * scale,
                cy + (sin(movement * 2f) * 8f).dp.toPx() * scale
            )
        )

        // Blob laterale destro, in opposizione al sinistro.
        drawCircle(
            color = color,
            radius = 15.dp.toPx() * scale,
            center = Offset(
                cx + (9f - sin(movement) * 8f).dp.toPx() * scale,
                cy + (sin(movement * 2f + 2f) * 8f).dp.toPx() * scale
            )
        )

        // Blob superiore.
        drawCircle(
            color = color,
            radius = 13.dp.toPx() * scale,
            center = Offset(
                cx + (sin(movement + 2f) * 10f).dp.toPx() * scale,
                cy + (-13f + sin(movement + 1f) * 7f).dp.toPx() * scale
            )
        )

        // Quarto blob che sale/scende lentamente per rendere
        // l'effetto più simile a una lava lamp.
        drawCircle(
            color = color,
            radius = 10.dp.toPx() * scale,
            center = Offset(
                cx + (sin(movement * 0.65f + 3f) * 11f).dp.toPx() * scale,
                cy + (sin(movement * 0.65f) * 17f).dp.toPx() * scale
            )
        )
    }
}




