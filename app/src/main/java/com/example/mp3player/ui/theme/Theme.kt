package com.example.mp3player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// Mantieni queste per Material3 (usate da componenti M3 interni)
private val DarkColorScheme = darkColorScheme(
    background = Dark_bg_main_views,
    surface = Dark_bg_main_views
)

private val LightColorScheme = lightColorScheme(
    background = Light_bg_main_views,
    surface = Light_bg_main_views
)

// Accessor globale — usalo nei composable: AppTheme.colors.bgMainViews
object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

@Composable
fun Mp3PlayerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val appColors   = if (darkTheme) DarkAppColors  else LightAppColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}