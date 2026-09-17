package com.example.mp3player.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LibraryMusic

import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.example.mp3player.data.model.MusicFolder
import com.example.mp3player.data.model.SubMusicFolder
import com.example.mp3player.ui.theme.AppTheme
import com.example.mp3player.ui.theme.AppColors
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Timer
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import com.example.mp3player.player.PlayerController
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.mutableStateListOf
import androidx.activity.compose.rememberLauncherForActivityResult
import android.net.Uri
import android.content.Intent
import android.provider.DocumentsContract
import android.os.Environment
import android.content.Context


sealed class SidebarSelection {
    object AllSongs : SidebarSelection()
    data class SubFolder(val path: String) : SidebarSelection()
}

fun uriToPath(context: Context, uri: Uri): String? {
    return try {
        val docId = DocumentsContract.getTreeDocumentId(uri)
        val split = docId.split(":")
        val type = split[0]
        if ("primary".equals(type, ignoreCase = true)) {
            if (split.size > 1) {
                "${Environment.getExternalStorageDirectory()}/${split[1]}"
            } else {
                Environment.getExternalStorageDirectory().toString()
            }
        } else {
            null // storage rimovibile/SD, gestione più complessa se serve
        }
    } catch (e: Exception) {
        null
    }
}
@Composable
fun FolderPickerDialog(
    onFolderSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    startPath: String = "/storage/emulated/0"
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    var currentPath by remember(startPath) { mutableStateOf(startPath) }
    val backStack = remember(startPath) { mutableStateListOf<String>() }
    val forwardStack = remember(startPath) { mutableStateListOf<String>() }

    val currentDir = remember(currentPath) { java.io.File(currentPath) }
    val subDirs = remember(currentPath) {
        currentDir.listFiles()
            ?.filter { it.isDirectory && !it.name.startsWith(".") }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()
    }

    fun navigateTo(newPath: String) {
        if (newPath == currentPath) return
        backStack.add(currentPath)
        forwardStack.clear()
        currentPath = newPath
    }

    fun goBack() {
        if (backStack.isNotEmpty()) {
            forwardStack.add(currentPath)
            currentPath = backStack.removeAt(backStack.lastIndex)
        }
    }

    fun goForward() {
        if (forwardStack.isNotEmpty()) {
            backStack.add(currentPath)
            currentPath = forwardStack.removeAt(forwardStack.lastIndex)
        }
    }

    // Launcher per il file picker nativo (Storage Access Framework)
    val nativePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val path = uriToPath(context, it) // la funzione che avevi già
            if (path != null) {
                onFolderSelected(path)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.bgMainViews,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentDir.name.ifEmpty { "Storage" },
                    color = colors.content,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { nativePickerLauncher.launch(null) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Apri picker di sistema",
                        tint = colors.content,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { goBack() },
                    enabled = backStack.isNotEmpty(),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Indietro",
                        tint = if (backStack.isNotEmpty()) colors.content
                        else colors.content.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { goForward() },
                    enabled = forwardStack.isNotEmpty(),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Avanti",
                        tint = if (forwardStack.isNotEmpty()) colors.content
                        else colors.content.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            LazyColumn {
                if (currentPath != startPath) {
                    item {
                        FolderPickerRow(
                            label = "..",
                            icon = Icons.Default.ArrowUpward,
                            colors = colors,
                            onClick = { navigateTo(currentDir.parent ?: startPath) }
                        )
                    }
                }
                items(subDirs) { dir ->
                    FolderPickerRow(
                        label = dir.name,
                        icon = Icons.Default.Folder,
                        colors = colors,
                        onClick = { navigateTo(dir.absolutePath) }
                    )
                }
                if (subDirs.isEmpty()) {
                    item {
                        Text(
                            text = "Nessuna sottocartella",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.content.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onFolderSelected(currentPath) }) {
                Text("Usa questa cartella", color = colors.interactive)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = colors.content.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
private fun FolderPickerRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: AppColors,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberCircleIndication(color = colors.interactive),
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = colors.content,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(text = label, color = colors.content, style = MaterialTheme.typography.bodyMedium)
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppSidebar(
    currentFolder: MusicFolder?,
    selection: SidebarSelection,
    onSelectionChange: (SidebarSelection) -> Unit,
    onFolderPicked: (String) -> Unit,
    onSubFolderDoubleClick: (SubMusicFolder) -> Unit,
    openFolderPicker: Boolean = false,
    folderPickerStartPath: String = "/storage/emulated/0",
    onFolderPickerOpened: () -> Unit = {},
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    playerController: PlayerController,
    modifier: Modifier = Modifier,
    statisticsOpen: Boolean = false,
    onToggleStatistics: () -> Unit = {},
) {
    val colors = AppTheme.colors
    var showFolderPicker by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showAppsPanel by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var timerRemainingMs by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(openFolderPicker) {
        if (openFolderPicker) {
            showFolderPicker = true
            onFolderPickerOpened()
        }
    }

    LaunchedEffect(timerRemainingMs) {
        while (timerRemainingMs != null && timerRemainingMs!! > 0) {
            delay(1000)
            timerRemainingMs = (timerRemainingMs!! - 1000).coerceAtLeast(0)
        }
        if (timerRemainingMs == 0L) {
            timerRemainingMs = null
            playerController.pause()
            (context as? Activity)?.finishAndRemoveTask()
        }
    }

    Box(
        modifier = modifier.fillMaxHeight()
    ) {
        // ── Sidebar principale (72dp, sempre visibile) ─────────
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(72.dp)
                .background(colors.bgSidebar)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 128.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // --- GRUPPO SUPERIORE ---
                SidebarIconButton(
                    selected = selection == SidebarSelection.AllSongs,
                    colors = colors,
                    onClick = { onSelectionChange(SidebarSelection.AllSongs) }
                ) {
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = "Tutti i brani",
                        tint = colors.content,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Cartelle sub-directory
                currentFolder?.subFolders?.forEach { sub ->
                    val isSubSelected = selection == SidebarSelection.SubFolder(sub.path)

                    SidebarIconButton(
                        selected = isSubSelected,
                        colors = colors,
                        modifier = Modifier.combinedClickable(
                            onClick = { onSelectionChange(SidebarSelection.SubFolder(sub.path)) },
                            onDoubleClick = { onSubFolderDoubleClick(sub) }
                        )
                    ) {
                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = sub.name,
                                tint = colors.content,
                                modifier = Modifier.size(28.dp)
                            )

                            Text(
                                text = sub.name.trim().firstOrNull()?.uppercase() ?: "",
                                color = if (colors.content.luminance() > 0.5f) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = 4.dp)
                            )
                        }
                    }
                }

                // --- LO SPAZIATORE MAGICO ---
                Spacer(modifier = Modifier.weight(1f))

                // --- GRUPPO INFERIORE ---
                HorizontalDivider(
                    color = colors.content.copy(alpha = 0.12f),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // icona Apps
                SidebarIconButton(
                    selected = showAppsPanel,
                    colors = colors,
                    onClick = { showAppsPanel = !showAppsPanel }
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Apps",
                        tint = colors.content
                    )
                }

                // icona tema
                SidebarIconButton(
                    selected = false,
                    colors = colors,
                    onClick = onToggleTheme
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = if (isDarkTheme) "Passa al tema chiaro" else "Passa al tema scuro",
                        tint = colors.content
                    )
                }
            } // Fine Column

            // I Dialog restano overlay dentro il Box principale
            if (showFolderPicker) {
                FolderPickerDialog(
                    startPath = folderPickerStartPath,
                    onFolderSelected = { path ->
                        showFolderPicker = false
                        onFolderPicked(path)
                    },
                    onDismiss = { showFolderPicker = false }
                )
            }

            if (showTimerDialog) {
                TimerDialog(
                    remainingMs = timerRemainingMs,
                    onSelectTime = { duration -> timerRemainingMs = duration },
                    onCancelTimer = { timerRemainingMs = null },
                    onDismiss = { showTimerDialog = false }
                )
            }
        } // Fine Box sidebar principale

        AnimatedVisibility(
            visible = showAppsPanel,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 72.dp, y = (-80).dp) // -80dp per stare sopra alle icone tema/apps
                .zIndex(20f)
        ) {
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.bgSidebar)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showAppsPanel = false
                            showFolderPicker = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Cartella",
                        tint = colors.content,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (timerRemainingMs != null) colors.selectedBg.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable {
                            showAppsPanel = false
                            showTimerDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = if (timerRemainingMs != null) colors.interactive else colors.content,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (statisticsOpen) colors.selectedBg.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable {
                            showAppsPanel = false
                            onToggleStatistics()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Statistiche",
                        tint = if (statisticsOpen) colors.interactive else colors.content,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    } // Fine Row esterno
}

@Composable
fun SidebarIconButton(
    selected: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (selected) colors.selectedBg
                else Color.Transparent
            )
            .then(
                if (selected) {
                    Modifier.border(
                        1.5.dp,
                        colors.selectedBorder,
                        CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = rememberCircleIndication(
                            color = colors.interactive
                        ),
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimerDialog(
    remainingMs: Long?,
    onSelectTime: (Long) -> Unit,
    onCancelTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors

    val timerOptions = listOf(
        "5 min" to 5 * 60 * 1000L,
        "10 min" to 10 * 60 * 1000L,
        "15 min" to 15 * 60 * 1000L,
        "30 min" to 30 * 60 * 1000L,
        "1 ora" to 60 * 60 * 1000L,
        "2 ore" to 2 * 60 * 60 * 1000L
    )

    AlertDialog(
        onDismissRequest = onDismiss,

        shape = RoundedCornerShape(20.dp),

        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.selectedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = colors.content
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Timer",
                        color = colors.content,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Quanto tempo vuoi impostare?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },

        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ─────────────────────────────
                // OPZIONI TIMER
                // ─────────────────────────────

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timerOptions.chunked(2).forEach { rowOptions ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowOptions.forEach { (label, duration) ->

                                TextButton(
                                    onClick = {
                                        onSelectTime(duration)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .border(
                                            width = 1.dp,
                                            color = colors.selectedBg.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = colors.content
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Completa la riga se c'è un solo elemento
                            if (rowOptions.size == 1) {
                                Spacer(
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // ─────────────────────────────
                // TIMER ATTIVO
                // ─────────────────────────────

                if (remainingMs != null) {

                    val totalSeconds =
                        (remainingMs / 1000).coerceAtLeast(0)

                    val hours = totalSeconds / 3600
                    val minutes = (totalSeconds % 3600) / 60
                    val seconds = totalSeconds % 60

                    val remainingText =
                        if (hours > 0) {
                            "%d:%02d:%02d".format(
                                hours,
                                minutes,
                                seconds
                            )
                        } else {
                            "%02d:%02d".format(
                                minutes,
                                seconds
                            )
                        }

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                colors.selectedBg.copy(alpha = 0.15f)
                            )
                            .border(
                                width = 1.dp,
                                color = colors.selectedBg.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .combinedClickable(
                                onClick = {},
                                onDoubleClick = onCancelTimer
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 14.dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = colors.selectedBg,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(6.dp)
                            )

                            Text(
                                text = "Timer attivo",
                                color = colors.content,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = remainingText,
                            color = colors.content,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Doppio click per annullare",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.selectedBg
                )
            ) {
                Text(
                    text = "Chiudi",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
