package com.example.mp3player.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mp3player.data.model.Song
import com.example.mp3player.data.model.SortDirection
import com.example.mp3player.data.model.SortField
import com.example.mp3player.ui.components.SearchBar
import com.example.mp3player.ui.components.SongListItem
import com.example.mp3player.ui.components.SortDirectionButton
import com.example.mp3player.ui.components.SortFieldButton
import com.example.mp3player.ui.theme.AppTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.background
import com.example.mp3player.ui.components.SwipeableSongItem
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.example.mp3player.ui.components.SongInfoSheet
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.SearchOff


@Composable
fun LibraryScreen(
    songs: List<Song> = emptyList(),
    viewModel: LibraryViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenFullscreen: () -> Unit,
    technicalPlayerEnabled: Boolean,
    onSortRevealRequest: (() -> Unit) -> Unit = {},
    onCloseSidebar: () -> Unit,
) {
    val colors = AppTheme.colors
    val playbackState by viewModel.playbackState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    var query         by remember { mutableStateOf("") }
    var sortField     by remember { mutableStateOf(SortField.TITLE) }
    var sortDirection by remember { mutableStateOf(SortDirection.ASC) }

    // ── Visibilità SortButton ──────────────────────────────
    var sortVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var sortHideJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }

    val hiddenIds by viewModel.hiddenIds.collectAsState()

    fun showSort() {
        sortVisible = true
        sortHideJob?.cancel()
        sortHideJob = scope.launch {
            delay(3_000)
            sortVisible = false
        }
    }

    // Espone showSort() a NavGraph (per il play/pause del PlayerBar)
    LaunchedEffect(Unit) {
        onSortRevealRequest { showSort() }
    }

    val sortAlpha by animateFloatAsState(
        targetValue = if (sortVisible) 1f else 0f,
        animationSpec = tween(200),
        label = "sortAlpha"
    )

    // ── FocusRequester per la SearchBar ───────────────────
    val searchFocusState = remember { androidx.compose.ui.focus.FocusRequester() }
    var searchHasFocus by remember { mutableStateOf(false) }

    val displayed = remember(songs, query, sortField, sortDirection) {
        val filtered = songs.filter { song ->
            query.isBlank() ||
                    song.title.contains(query, ignoreCase = true) ||
                    song.artist.contains(query, ignoreCase = true)
        }
        val comparator = when (sortField) {
            SortField.TRACK_NUMBER -> compareBy<Song> { it.album.lowercase() }.thenBy { it.trackNumber }
            SortField.TITLE  -> compareBy { it.title.lowercase() }
            SortField.ARTIST -> compareBy { it.artist.lowercase() }
            SortField.ALBUM  -> compareBy { it.album.lowercase() }
        }
        if (sortDirection == SortDirection.ASC) filtered.sortedWith(comparator)
        else filtered.sortedWith(comparator.reversed())
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgMainViews)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.weight(1f),
                onFocusChanged = { searchHasFocus = it }  // vedi nota sotto
            )

            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .graphicsLayer { alpha = if (searchHasFocus) 1f else sortAlpha }
            ) {
                SortFieldButton(
                    current = sortField,
                    onSelect = {
                        sortField = it
                        showSort()
                    },
                    onOpen = { showSort() }
                )
            }

            Box(
                modifier = Modifier
                    .padding(end = 20.dp)
                    .graphicsLayer { alpha = if (searchHasFocus) 1f else sortAlpha }
            ) {
                SortDirectionButton(
                    direction = sortDirection,
                    onToggle = {
                        sortDirection = if (sortDirection == SortDirection.ASC)
                            SortDirection.DESC else SortDirection.ASC
                        showSort()
                    }
                )
            }
        }

        if (displayed.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (query.isBlank()) Icons.Default.MusicOff else Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = colors.content.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = if (query.isBlank()) "Nessuna canzone trovata"
                        else "Nessun risultato per \"$query\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.content.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (query.isBlank()) "Seleziona una cartella dal menu laterale"
                        else "Prova con un termine diverso",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.content.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start  = 16.dp,
                    end    = 16.dp,
                    top    = 4.dp,
                    bottom = 80.dp   // spazio sotto l'ultima canzone
                )
            ) {
                items(displayed, key = { it.id }) { song ->
                    val isHidden = song.id in hiddenIds

                    Box(
                        modifier = Modifier
                            .alpha(if (isHidden) 0.35f else 1f)
                        // Niente pointerInput qui
                    ) {
                        SwipeableSongItem(
                            song              = song,
                            isPlaying         = playbackState.currentSong?.id == song.id,
                            isFavorite        = favorites.any { it.id == song.id },
                            onClick = { if (!isHidden) viewModel.onSongClick(song, displayed) },
                            onLongClick       = {
                                // Questo funziona sempre, anche se nascosto
                                onCloseSidebar()
                                selectedSong = song
                            },
                            onFavoriteToggle  = { if (!isHidden) viewModel.onFavoriteToggle(song) },
                            onAddToQueue      = { if (!isHidden) viewModel.onAddToQueue(song) },
                            isFavoriteEnabled = !isHidden,
                            swipeEnabled      = !isHidden
                        )
                    }
                }
            }
        }
        selectedSong?.let { song ->
            SongInfoSheet(
                song             = song,
                isFavorite       = favorites.any { it.id == song.id },
                isHidden         = viewModel.isHidden(song),
                onDismiss        = { selectedSong = null },
                onFavoriteToggle = { viewModel.onFavoriteToggle(song) },
                onAddToQueue     = { viewModel.onAddToQueue(song) },
                playCount        = viewModel.getPlayCount(song),
                onHideSong       = { viewModel.onHideSongToggle(it) }
            )
        }
    }
}