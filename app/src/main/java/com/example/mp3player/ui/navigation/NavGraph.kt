package com.example.mp3player.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.mp3player.Mp3PlayerApp
import com.example.mp3player.data.model.MusicFolder
import com.example.mp3player.data.model.Song
import com.example.mp3player.ui.components.AppBottomNavBar
import com.example.mp3player.ui.components.AppSidebar
import com.example.mp3player.ui.components.AppTopBar
import com.example.mp3player.ui.components.PlayerBar
import com.example.mp3player.ui.screens.fullscreen.TechnicalPlayerOverlay
import com.example.mp3player.ui.components.SidebarSelection
import com.example.mp3player.ui.screens.favorites.FavoritesScreen
import com.example.mp3player.ui.screens.favorites.FavoritesViewModel
import com.example.mp3player.ui.screens.library.LibraryScreen
import com.example.mp3player.ui.screens.library.LibraryViewModel
import com.example.mp3player.ui.screens.queue.QueueScreen
import com.example.mp3player.ui.screens.queue.QueueViewModel
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.example.mp3player.ui.navigation.TechnicalPlayerContainer
import kotlinx.coroutines.launch
import com.example.mp3player.data.local.datastore.UserPreferencesDataStore
import com.example.mp3player.ui.components.SongInfoSheet
import com.example.mp3player.ui.screens.statistics.StatisticsScreen
import com.example.mp3player.ui.screens.statistics.StatisticsViewModel
private val SIDEBAR_WIDTH = 72.dp

@Composable
fun NavGraph(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val container =
        (context.applicationContext as Mp3PlayerApp).container

    val playerController = container.playerController
    val queueRepository = container.queueRepository
    val favoritesRepository = container.favoritesRepository
    val musicRepository = container.musicRepository
    val prefsRepo = container.preferencesRepository

    var swipeX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 60.dp  // soglia in dp per aprire
    /*
     * Indica che abbiamo già completato il tentativo
     * di ripristino iniziale.
     */
    var restoredFromPrefs by remember {
        mutableStateOf(false)
    }

    /*
     * Quando true, AppSidebar apre automaticamente
     * il nostro FolderPickerDialog.
     */
    var openFolderPicker by remember {
        mutableStateOf(false)
    }

    var revealSortButtons by remember {
        mutableStateOf<(() -> Unit)?>(null)
    }

    /*
     * Cartella dalla quale deve partire il picker
     * quando non riusciamo a ripristinare quella salvata.
     */
    val fallbackPickerPath =
        "/storage/emulated/0/Music/Favourite"

    val libraryViewModel = remember {
        LibraryViewModel(
            playerController,
            queueRepository,
            favoritesRepository,
            container.preferencesRepository,
            container.hiddenSongsRepository,
            container.playStatsRepository
        )
    }

    val scope = rememberCoroutineScope()

    val navController = rememberNavController()

    var sidebarOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var currentFolder by remember {
        mutableStateOf<MusicFolder?>(null)
    }

    val queueViewModel = remember {
        QueueViewModel(
            queueRepository,
            favoritesRepository,
            playerController,
        )
    }

    val favoritesViewModel = remember {
        FavoritesViewModel(
            favoritesRepository,
            queueRepository,
            playerController,
            container.playStatsRepository
        )
    }

    val statisticsViewModel = remember {
        StatisticsViewModel(
            container.playStatsRepository,
            playerController,
        )
    }

    var selection by remember {
        mutableStateOf<SidebarSelection>(
            SidebarSelection.AllSongs
        )
    }

    val prefs by prefsRepo.preferences.collectAsState(
        initial = com.example.mp3player.data.local.datastore.UserPreferences()
    )

    LaunchedEffect(prefs.currentFolderPath) {
        android.util.Log.d(
            "NavGraph",
            "prefs aggiornato, currentFolderPath = ${prefs.currentFolderPath}"
        )
    }

    var showFullscreenPlayer by remember {
        mutableStateOf(false)
    }

    // Stato TechnicalPlayer — aperto solo dalla LibraryScreen via PlayerBar globale
    var showTechnicalPlayer by remember {
        mutableStateOf(false)
    }

    var technicalPlayerEnabled by remember { mutableStateOf(true) }

//    LaunchedEffect(prefs.technicalPlayerEnabled) {
//        technicalPlayerEnabled = prefs.technicalPlayerEnabled
//    }

    val onToggleTechnicalPlayerEnabled: (Boolean) -> Unit = { enabled ->
        technicalPlayerEnabled = enabled
        if (!enabled) showTechnicalPlayer = false
    }

    var showCurrentSongInfo by remember {
        mutableStateOf(false)
    }

    val sidebarWidth by animateDpAsState(
        targetValue = if (sidebarOpen) {
            SIDEBAR_WIDTH
        } else {
            0.dp
        },
        animationSpec = tween(220),
        label = "sidebarWidth"
    )

    val hiddenIds by libraryViewModel.hiddenIds.collectAsState()
    val visibleSongs: List<Song> =
        remember(currentFolder, selection) {

            val folder =
                currentFolder
                    ?: return@remember emptyList()

            when (val sel = selection) {

                SidebarSelection.AllSongs -> {
                    folder.directSongs +
                            folder.subFolders.flatMap {
                                it.songs
                            }
                }

                is SidebarSelection.SubFolder -> {
                    folder.subFolders
                        .firstOrNull {
                            it.path == sel.path
                        }
                        ?.songs
                        ?: emptyList()
                }
            }
        }

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry?.destination?.route

    val currentTitle =
        BottomNavItems
            .find {
                it.route == currentRoute
            }
            ?.title
            ?: "Statistiche"

    // Stato playback per il PlayerBar globale
    val playbackState by playerController.state.collectAsState()

    var statisticsOpen by remember { mutableStateOf(false) }

    val closeStatistics: () -> Unit = {
        statisticsOpen = false
        navController.navigate(Screen.Library.route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Row(
            modifier = Modifier.fillMaxSize()
        ) {

            // Spaziatore: la sidebar vera ora è un overlay più sotto,
            // questo Spacer serve solo a spingere il contenuto a destra.
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.width(sidebarWidth)
            )

            Box(
                modifier = Modifier
                    .weight(1f / 7f)
                    .pointerInput(sidebarOpen) {
                        detectHorizontalDragGestures(
                            onDragStart = { swipeX = 0f },
                            onDragCancel = { swipeX = 0f },
                            onHorizontalDrag = { change, dragAmount -> change.consume(); swipeX += dragAmount },
                            onDragEnd = {
                                val threshold = swipeThreshold.toPx()
                                if (!sidebarOpen && swipeX > threshold) sidebarOpen = true
                                else if (sidebarOpen && swipeX < -threshold) sidebarOpen = false
                                swipeX = 0f
                            }
                        )
                    }
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                    bottomBar = {
                        Column {
                            if (!showFullscreenPlayer) {
                                val current = playbackState.currentSong
                                PlayerBar(
                                    isPlaying        = playbackState.isPlaying,
                                    songTitle        = current?.title ?: "Nessun brano",
                                    artistName       = current?.artist ?: "",
                                    albumArtUri      = current?.albumArtUri,
                                    progressFraction = if (current != null && current.duration > 0)
                                        (playbackState.currentPositionMs.toFloat() / current.duration.toFloat()).coerceIn(0f, 1f)
                                    else 0f,
                                    currentTime      = formatMillis(playbackState.currentPositionMs),
                                    totalTime        = formatMillis(current?.duration ?: 0L),
                                    durationMs       = current?.duration ?: 0L,
                                    shuffleEnabled   = playbackState.isShuffleEnabled,
                                    repeatEnabled    = playbackState.isRepeatEnabled,
                                    onPlayPause = {
                                        libraryViewModel.onPlayPause()
                                        revealSortButtons?.invoke()
                                    },
                                    onShuffleToggle  = { libraryViewModel.onShuffleToggle(visibleSongs) },
                                    onRepeatToggle   = { libraryViewModel.onRepeatToggle() },
                                    onSeek           = { libraryViewModel.onSeek(it) },
                                    onSwipeNext      = { libraryViewModel.onSwipeNext() },
                                    onSwipePrevious  = { libraryViewModel.onSwipePrevious() },
                                    onTechnicalOpen = {
                                        if (technicalPlayerEnabled) {
                                            showTechnicalPlayer = true
                                        }
                                    },
                                    onFullscreenOpen = { showFullscreenPlayer = true },
                                    onOpenFullscreen = { showFullscreenPlayer = true },
                                    onLongPress = { showCurrentSongInfo = true },
                                )
                            }
                            AppBottomNavBar(
                                navController = navController,
                                isPlaying = playbackState.isPlaying,
                                showStatisticsTab = statisticsOpen
                            )
                        }

                        val playStats by container.playStatsRepository.playStats.collectAsState()

                        val currentSong = playbackState.currentSong
                        if (showCurrentSongInfo && currentSong != null) {
                            SongInfoSheet(
                                song             = currentSong,
                                isFavorite       = libraryViewModel.isFavorite(currentSong),
                                isHidden         = libraryViewModel.isHidden(currentSong),
                                playCount        = playStats.find { it.song.id == currentSong.id }?.playCount,
                                onDismiss        = { showCurrentSongInfo = false },
                                onFavoriteToggle = { libraryViewModel.onFavoriteToggle(currentSong) },
                                onAddToQueue     = { libraryViewModel.onAddToQueue(currentSong) },
                                onHideSong       = { libraryViewModel.onHideSongToggle(currentSong) }
                            )
                        }
                    }

                ) { padding ->

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Library.route,
                        modifier = Modifier.padding(
                            top = 100.dp,
                            bottom = padding.calculateBottomPadding()
                        )
                    ) {
                        composable(Screen.Library.route) {
                            LibraryScreen(
                                songs = visibleSongs,
                                viewModel = libraryViewModel,
                                onOpenFullscreen = { showFullscreenPlayer = true },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = onToggleTheme,
                                technicalPlayerEnabled = prefs.technicalPlayerEnabled,
                                onSortRevealRequest = { fn -> revealSortButtons = fn },
                                onCloseSidebar = { sidebarOpen = false },
                            )
                        }
                        composable(Screen.Favorites.route) {
                            FavoritesScreen(
                                favoritesViewModel,
                                onCloseSidebar = { sidebarOpen = false },
                            )
                        }
                        composable(Screen.Queue.route) {
                            QueueScreen(queueViewModel)
                        }
                        composable(Screen.Statistics.route) {
                            StatisticsScreen(statisticsViewModel, onClose = closeStatistics)
                        }
                    }
                }
            }
        }

        // ── Sidebar: overlay indipendente, sempre sopra topbar/navbar/player ──
        AppSidebar(
            modifier = Modifier
                .width(sidebarWidth)
                .fillMaxHeight()
                .zIndex(10f),

            currentFolder = currentFolder,
            selection = selection,
            playerController = playerController,

            onSelectionChange = {
                selection = it
                navController.navigate(Screen.Library.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },

            onFolderPicked = { path ->
                val loaded = musicRepository.loadFolder(path)
                currentFolder = loaded
                selection = SidebarSelection.AllSongs
                restoredFromPrefs = true
                navController.navigate(Screen.Library.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },

            onSubFolderDoubleClick = { sub ->
                val playable = sub.songs.filter { !libraryViewModel.isHidden(it) }
                if (playable.isNotEmpty()) {
                    playerController.play(playable.random(), playable)
                    selection = SidebarSelection.SubFolder(sub.path)
                }
            },

            openFolderPicker = openFolderPicker,
            folderPickerStartPath = fallbackPickerPath,
            onFolderPickerOpened = { openFolderPicker = false },

            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,

            statisticsOpen = statisticsOpen,
            onToggleStatistics = {
                if (statisticsOpen) {
                    closeStatistics()
                } else {
                    statisticsOpen = true
                    navController.navigate(Screen.Statistics.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
        )

        if (!showFullscreenPlayer && !showTechnicalPlayer) {
            AppTopBar(
                currentTitle = currentTitle,
                sidebarOpen = sidebarOpen,

                selectedFolderName = when (val currentSelection = selection) {
                    SidebarSelection.AllSongs -> currentFolder?.name
                    is SidebarSelection.SubFolder -> currentFolder?.subFolders
                        ?.firstOrNull { it.path == currentSelection.path }
                        ?.name
                    else -> null
                },

                onMenuClick = { sidebarOpen = !sidebarOpen },

                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,

                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(15f)
            )
        }

        TechnicalPlayerContainer(
            visible = showTechnicalPlayer,
            modifier = Modifier.zIndex(20f),
            playbackState = playbackState,
            onClose = { showTechnicalPlayer = false },
            onPlayPause = { libraryViewModel.onPlayPause() },
            onSkipNext = { libraryViewModel.onSkipNext() },
            onSkipPrevious = { libraryViewModel.onSkipPrevious() },
            onSeekForward10 = { libraryViewModel.onSeekForward10() },
            onSeekBack10 = { libraryViewModel.onSeekBack10() },
            onShuffleToggle = { libraryViewModel.onShuffleToggle(visibleSongs) },
            onRepeatToggle = { libraryViewModel.onRepeatToggle() },
            onSeek = { libraryViewModel.onSeek(it) },
            onOpenFullscreen = {
                showTechnicalPlayer = false
                showFullscreenPlayer = true
            },
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme
        )

        FullscreenContainer(
            visible = showFullscreenPlayer,
            modifier = Modifier.zIndex(25f),

            playbackState = playerController.state.collectAsState().value,

            onClose = { showFullscreenPlayer = false },
            onPlayPause = { libraryViewModel.onPlayPause() },
            onSkipNext = { libraryViewModel.onSkipNext() },
            onSkipPrevious = { libraryViewModel.onSkipPrevious() },

            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,

            onSeek = { positionMs ->
                libraryViewModel.onSeek(positionMs)
            },

            onTechnicalOpen = {
                if (technicalPlayerEnabled) {
                    showTechnicalPlayer = true
                }
                showFullscreenPlayer = false
            },

            onShuffleToggle = { libraryViewModel.onShuffleToggle(visibleSongs) },
            onRepeatToggle = { libraryViewModel.onRepeatToggle() },

            onSeekRelative = { millis ->
                libraryViewModel.seekRelative(millis)
            }
        )
    }

    // ─────────────────────────────────────────────────────────
    // 1. RIPRISTINO DELLA CARTELLA
    // ─────────────────────────────────────────────────────────

    LaunchedEffect(prefs.currentFolderPath) {

        val autoOpenFolderPicker = false

        if (restoredFromPrefs) {
            return@LaunchedEffect
        }

        val savedPath =
            prefs.currentFolderPath

        /*
         * Abbiamo una cartella precedentemente salvata.
         */
        if (!savedPath.isNullOrBlank()) {

            android.util.Log.d(
                "NavGraph",
                "Tentativo ripristino cartella: $savedPath"
            )

            val loaded =
                musicRepository.loadFolder(savedPath)

            android.util.Log.d(
                "NavGraph",
                "Cartella caricata: ${loaded.name}, " +
                        "directSongs=${loaded.directSongs.size}, " +
                        "subFolders=${loaded.subFolders.size}"
            )

            /*
             * Consideriamo valido il caricamento se
             * troviamo almeno un brano.
             */
            val hasSongs =
                loaded.directSongs.isNotEmpty() ||
                        loaded.subFolders.any {
                            it.songs.isNotEmpty()
                        }

            if (hasSongs) {

                currentFolder = loaded

                selection = when {

                    prefs.currentSelection == "all" -> {
                        SidebarSelection.AllSongs
                    }

                    prefs.currentSelection.startsWith("sub:") -> {
                        SidebarSelection.SubFolder(
                            prefs.currentSelection
                                .removePrefix("sub:")
                        )
                    }

                    else -> {
                        SidebarSelection.AllSongs
                    }
                }

                restoredFromPrefs = true

                android.util.Log.d(
                    "NavGraph",
                    "Cartella ripristinata correttamente"
                )

                return@LaunchedEffect
            }

            /*
             * Il path esiste magari ancora, ma non contiene
             * più musica leggibile.
             */
            android.util.Log.d(
                "NavGraph",
                "Cartella salvata non valida o vuota"
            )
        }

        /*
         * /storage/emulated/0/Music/Favourite
         */
        android.util.Log.d(
            "NavGraph",
            "Apro automaticamente FolderPicker"
        )

        if (autoOpenFolderPicker) {
            openFolderPicker = true
        }
    }

    // ─────────────────────────────────────────────────────────
    // 2. SALVATAGGIO CARTELLA + SELEZIONE
    // ─────────────────────────────────────────────────────────

    LaunchedEffect(
        currentFolder,
        selection
    ) {

        if (!restoredFromPrefs) {
            return@LaunchedEffect
        }

        val path =
            currentFolder?.path
                ?: return@LaunchedEffect

        android.util.Log.d(
            "NavGraph",
            "Salvo nel DataStore: " +
                    "path=$path, selection=$selection"
        )

        val sel =
            when (val s = selection) {

                SidebarSelection.AllSongs -> {
                    "all"
                }

                is SidebarSelection.SubFolder -> {
                    "sub:${s.path}"
                }
            }

        prefsRepo.saveFolderAndSelection(
            path,
            sel
        )
    }

    // ─────────────────────────────────────────────────────────
    // 3. RIPRISTINO ULTIMA CANZONE
    // ─────────────────────────────────────────────────────────

    LaunchedEffect(
        prefs.currentSongId,
        currentFolder
    ) {

        val songId =
            prefs.currentSongId
                ?: return@LaunchedEffect

        val folder =
            currentFolder
                ?: return@LaunchedEffect

        val allSongs =
            folder.directSongs +
                    folder.subFolders.flatMap {
                        it.songs
                    }

        val song =
            allSongs.firstOrNull {
                it.id == songId
            }
                ?: return@LaunchedEffect

        val nonHiddenSongs = allSongs.filter { it.id !in hiddenIds }
        playerController.play(song, nonHiddenSongs)
        playerController.seekTo(prefs.currentPositionMs)

        playerController.setShuffleEnabled(
            prefs.shuffleEnabled,
            visibleSongs
        )

        playerController.setRepeatEnabled(
            prefs.repeatEnabled
        )
    }
}

private fun formatMillis(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}