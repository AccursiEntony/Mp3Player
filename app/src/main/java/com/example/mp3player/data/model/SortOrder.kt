package com.example.mp3player.data.model

/** I modi in cui una lista può essere ordinata e raggruppata. */
enum class SortField {
    TRACK_NUMBER, TITLE, ARTIST, ALBUM
}

enum class SortDirection {
    ASC, DESC
}

data class SortOrder(
    val field: SortField = SortField.TITLE,
    val direction: SortDirection = SortDirection.ASC
)