package com.example.mp3player.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.mp3player.ui.theme.AppTheme
import com.example.mp3player.ui.components.AppLogo
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentTitle: String,
    sidebarOpen: Boolean,
    onMenuClick: () -> Unit,
    selectedFolderName: String? = null,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val systemInDarkTheme = isSystemInDarkTheme()
    val themeMismatch = isDarkTheme != systemInDarkTheme

    val statusBarGradient = if (themeMismatch) {
        Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    colors.content.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = 100f // altezza approssimativa dell'area status bar + top bar
            )
        )
    } else {
        Modifier
    }

    TopAppBar(
//        modifier = modifier.then(statusBarGradient),
        modifier = modifier,
        title = {
            Text(
                text = if (currentTitle == "Library" && selectedFolderName != null) {
                    "Library ($selectedFolderName)"
                } else {
                    currentTitle
                },
                color = colors.content,
                fontFamily = FontFamily.Serif,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 8.dp
                )
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = colors.content,
            navigationIconContentColor = colors.content
        ),
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 8.dp
                )
            ) {
                Icon(
                    imageVector = if (sidebarOpen) {
                        Icons.Filled.ChevronLeft
                    } else {
                        Icons.Filled.ChevronRight
                    },
                    contentDescription = if (sidebarOpen) {
                        "Chiudi sidebar"
                    } else {
                        "Apri sidebar"
                    },
                    tint = colors.content,
                    modifier = Modifier.size(32.dp)
                )
            }
        },

        actions = {
            AppLogo(
                onClick = onToggleTheme,
                modifier = Modifier
                    .padding(end = 40.dp, top = 8.dp)
                    .size(24.dp)
            )
        }
    )
}
