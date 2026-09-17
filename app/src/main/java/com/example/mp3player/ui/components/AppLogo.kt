package com.example.mp3player.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

@Composable
fun AppLogo(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFA8C8E8),
            Color(0xFFA8C8E8),
            Color(0xFF8B4A6B),
            Color(0xFF7A1030)
        )
    )
    Canvas(
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        val barCount = 5
        val gap = size.width / 9f
        val barWidth = gap
        val heights = listOf(0.65f, 1f, 0.55f, 0.9f, 0.6f)
        heights.forEachIndexed { i, h ->
            val x = gap * (i * 2)
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, size.height * (1 - h) / 2),
                size = Size(barWidth, size.height * h),
                cornerRadius = CornerRadius(barWidth / 2)
            )
        }
    }
}
