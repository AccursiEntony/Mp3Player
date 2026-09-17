package com.example.mp3player.ui.navigation

sealed class Screen(val route: String) {

    object Library : Screen("library")

    object Favorites : Screen("favorites")

    object Queue : Screen("queue")

    object Statistics : Screen("statistics")

}