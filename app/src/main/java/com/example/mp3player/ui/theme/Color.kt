package com.example.mp3player.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────
//  DARK THEME
// ─────────────────────────────────────────────

// Sfondi
// Toni molto scuri con leggere variazioni di luminosità,
// così ogni superficie dell'interfaccia rimane distinguibile.
val Dark_bg_bottom_navbar   = Color(0xFF09090E) // Sfondo barra di navigazione inferiore
val Dark_bg_player_bar      = Color(0xFF1B1B23) // Sfondo barra del riproduttore
val Dark_bg_sidebar         = Color(0xFF0C0C12) // Sfondo barra laterale
val Dark_bg_main_views      = Color(0xFF111117) // Sfondo schermate principali e selettore file
val Dark_bg_fullscreen      = Color(0xFF050507) // Sfondo visualizzazione a schermo intero
val Dark_menu_info_bg       = Color(0xFF17171F) // Sfondo del menu informazioni


// Elementi Interagibili e Stati
// Il blu è stato reso più netto e meno tendente al cyan,
// così da distinguerlo chiaramente dal colore "detail".
val Dark_y_interactive = Color(0xFF0A5FC4) // blu pieno, saturo
val Dark_y_selected    = Color(0xFF8ECBFF) // azzurro chiaro, alto contrasto su sfondo scuro


// Dettagli e Accenti
val Dark_z_detail             = Color(0xFF00E6C3) // Accenti e dettagli decorativi
val Dark_t_content            = Color(0xFFF1F3F5) // Testo e icone in stato predefinito


// Preferiti
// Il rosa/rosso rimane separato dal colore principale dell'app,
// rendendo immediatamente riconoscibile la funzione preferiti.
val Dark_fav_unselected     = Color(0xFFFF8FA8) // Icona preferiti non selezionata
val Dark_fav_selected       = Color(0xFFFF174F) // Icona preferiti selezionata


// Contrasti e Ombre
val Dark_contrast_border    = Color(0xFFE1E3E6) // Bordo del riproduttore ad alto contrasto
val Dark_contrast_color     = Color(0xFF1B1B23) // Testo e icone ad alto contrasto
val Dark_shadow_color       = Color(0xBAE7E7E7) // Ombra della barra del riproduttore

// Progress Bar (usato anche per swipeableSongItem)
val Dark_star_progressBar = Color(0xFF00E6C3)

val Dark_end_progressBar = Color(0xFF5B8CFF)


// ─────────────────────────────────────────────
//  LIGHT THEME
// ─────────────────────────────────────────────

// Sfondi
// Il tema chiaro mantiene la stessa gerarchia del dark theme,
// utilizzando superfici chiare con differenze sottili.
val Light_bg_bottom_navbar  = Color(0xFFF8F9FA) // Sfondo barra di navigazione inferiore
val Light_bg_player_bar     = Color(0xFFE2E6EA) // Sfondo barra del riproduttore
val Light_bg_sidebar        = Color(0xFFF9FCFF) // Sfondo barra laterale
val Light_bg_main_views     = Color(0xFFFFFFFF) // Sfondo schermate principali e selettore file
val Light_bg_fullscreen     = Color(0xFFE9ECEF) // Sfondo visualizzazione a schermo intero
val Light_menu_info_bg      = Color(0xFFF2F4F7) // Sfondo del menu informazioni


// Elementi Interagibili e Stati
// Blu più profondo rispetto al dark theme per mantenere
// un buon contrasto sui fondi chiari.
val Light_y_interactive       = Color(0xFF0077B6) // Stato hover e focused
val Light_y_selected          = Color(0xFFA5D9FD) // Bordo e sfondo dello stato selezionato


// Dettagli e Accenti
// Il colore viene mantenuto scuro per garantire leggibilità
// sullo sfondo bianco.
val Light_z_detail            = Color(0xFF1565C0) // Accenti e dettagli decorativi
val Light_t_content           = Color(0xFF1A1A24) // Testo e icone in stato predefinito


// Preferiti
val Light_fav_unselected    = Color(0xFFFFCCD5) // Icona preferiti non selezionata
val Light_fav_selected      = Color(0xFFE63946) // Icona preferiti selezionata


// Contrasti e Ombre
val Light_contrast_border   = Color(0xFF1C1B1F) // Bordo del riproduttore ad alto contrasto
val Light_contrast_color    = Color(0xFFE2E6EA) // Testo e icone ad alto contrasto
val Light_shadow_color      = Color(0xFF000000) // Ombra della barra del riproduttore

// Progress Bar (usato anche per swipeableSongItem)
val Light_star_progressBar = Color(0xFF5B8CFF)

val Light_end_progressBar = Color(0xFF00E6C3)


// ─────────────────────────────────────────────
//  PODIO STATISTICHE
// ─────────────────────────────────────────────

// Colori semantici indipendenti dalla palette principale.
val Podium_gold            = Color(0xFFD4AF37) // 1° Posto
val Podium_silver           = Color(0xFFB0B0B8) // 2° Posto
val Podium_bronze            = Color(0xFFCD7F32) // 3° Posto