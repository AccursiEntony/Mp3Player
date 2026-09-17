package com.example.mp3player.ui.screens.library

import com.example.mp3player.data.model.SortDirection
import com.example.mp3player.data.model.SortField

data class LibraryUiState(
    val query: String = "",
    val sortField: SortField = SortField.TITLE,
    val sortDirection: SortDirection = SortDirection.ASC
)