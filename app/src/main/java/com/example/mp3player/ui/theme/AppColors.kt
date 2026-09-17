package com.example.mp3player.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(

    // ── Sfondi ──────────────────────────────
    val bgBottomNavbar  : Color,
    val bgPlayerBar     : Color,
    val bgSidebar       : Color,
    val bgMainViews     : Color,
    val bgFullscreen    : Color,

    // ── Elementi interagibili ────────────────
    val interactive     : Color,
    val selectedBorder  : Color,
    val selectedBg      : Color,

    // ── Dettagli ────────────────────────────
    val detail          : Color,

    // ── Testi e Icone ────────────────────────
    val content         : Color,

    // ── Preferiti ───────────────────────────
    val favUnselected   : Color,
    val favSelected     : Color,

    val contrast_border : Color,
    val contrast_color: Color,

    val shadow_color: Color,

    val menuInfoBg: Color,
    var gray: Color,

    // ── Podio statistiche ───────────────────
    val podiumGold      : Color,
    val podiumSilver    : Color,
    val podiumBronze    : Color,

    // --- Progress Bar ------
    val startProgessBar : Color,
    val endProgressBar: Color,
)

val DarkAppColors = AppColors(
    bgBottomNavbar  = Dark_bg_bottom_navbar,
    bgPlayerBar     = Dark_bg_player_bar,
    bgSidebar       = Dark_bg_sidebar,
    bgMainViews     = Dark_bg_main_views,
    bgFullscreen    = Dark_bg_fullscreen,
    interactive     = Dark_y_interactive,
    selectedBorder  = Dark_y_selected,
    selectedBg      = Dark_y_selected,
    detail          = Dark_z_detail,
    content         = Dark_t_content,
    favUnselected   = Dark_fav_unselected,
    favSelected     = Dark_fav_selected,
    contrast_border = Dark_contrast_border,
    contrast_color = Dark_contrast_color,
    shadow_color = Dark_shadow_color,
    menuInfoBg = Dark_menu_info_bg,
    gray = Color(0xFF808080),
    podiumGold = Podium_gold,
    podiumSilver = Podium_silver,
    podiumBronze = Podium_bronze,
    startProgessBar = Dark_star_progressBar,
    endProgressBar = Dark_end_progressBar
)

val LightAppColors = AppColors(
    bgBottomNavbar  = Light_bg_bottom_navbar,
    bgPlayerBar     = Light_bg_player_bar,
    bgSidebar       = Light_bg_sidebar,
    bgMainViews     = Light_bg_main_views,
    bgFullscreen    = Light_bg_fullscreen,
    interactive     = Light_y_interactive,
    selectedBorder  = Light_y_selected,
    selectedBg      = Light_y_selected,
    detail          = Light_z_detail,
    content         = Light_t_content,
    favUnselected   = Light_fav_unselected,
    favSelected     = Light_fav_selected,
    contrast_border = Light_contrast_border,
    contrast_color = Light_contrast_color,
    shadow_color    = Light_shadow_color,
    menuInfoBg = Light_menu_info_bg,
    gray = Color(0xFF808080),
    podiumGold = Podium_gold,
    podiumSilver = Podium_silver,
    podiumBronze = Podium_bronze,
    startProgessBar = Light_star_progressBar,
    endProgressBar = Light_end_progressBar
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
