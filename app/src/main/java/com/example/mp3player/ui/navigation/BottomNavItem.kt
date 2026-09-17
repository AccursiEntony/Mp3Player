package com.example.mp3player.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.BarChart

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

val BottomNavItems = listOf(
    BottomNavItem(
        title = "Favorites",
        icon = Icons.Default.Favorite,
        route = Screen.Favorites.route
    ),
    BottomNavItem(
        title = "Library",
        icon = Icons.Default.LibraryMusic,
        route = Screen.Library.route
    ),
    BottomNavItem(
        title = "Queue",
        icon = Icons.Default.QueueMusic,
        route = Screen.Queue.route
    ),
)

val StatisticsNavItem = BottomNavItem(
    title = "Statistics",
    icon = Icons.Default.BarChart,
    route = Screen.Statistics.route
)