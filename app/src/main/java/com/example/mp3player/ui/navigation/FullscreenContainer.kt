package com.example.mp3player.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.mp3player.player.PlaybackState
import com.example.mp3player.ui.screens.fullscreen.FullscreenPlayerOverlay
import com.example.mp3player.ui.screens.fullscreen.TechnicalPlayerOverlay


@Composable
fun FullscreenContainer(
    visible: Boolean,
    playbackState: PlaybackState,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onShuffleToggle: () -> Unit,
    onSeekRelative: (Long) -> Unit,
    onRepeatToggle: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onTechnicalOpen: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter   = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit    = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        FullscreenPlayerOverlay(
            playbackState = playbackState,
            onClose = onClose,
            onTechnicalOpen = onTechnicalOpen,
            onPlayPause = onPlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onShuffleToggle = onShuffleToggle,
            onRepeatToggle = onRepeatToggle,
            onSeekRelative = onSeekRelative,
            isDarkTheme = isDarkTheme,
            onSeek = onSeek,
            onToggleTheme = onToggleTheme
        )
    }
}

@Composable
fun TechnicalPlayerContainer(
    visible: Boolean,
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
    onOpenFullscreen: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        TechnicalPlayerOverlay(
            playbackState = playbackState,
            onClose = onClose,
            onPlayPause = onPlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onSeekForward10 = onSeekForward10,
            onSeekBack10 = onSeekBack10,
            onShuffleToggle = onShuffleToggle,
            onRepeatToggle = onRepeatToggle,
            onSeek = onSeek,
            onOpenFullscreen = onOpenFullscreen,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )
    }
}