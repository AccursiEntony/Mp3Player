package com.example.mp3player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mp3player.data.model.SortDirection
import com.example.mp3player.data.model.SortField
import com.example.mp3player.ui.theme.AppTheme

@Composable
fun SortFieldButton(
    current: SortField,
    onSelect: (SortField) -> Unit,
    onOpen: () -> Unit = {}
) {
    val colors = AppTheme.colors
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val labels = mapOf(
        SortField.TRACK_NUMBER to "Indice",
        SortField.TITLE        to "Titolo",
        SortField.ARTIST       to "Artista",
        SortField.ALBUM        to "Album",
    )

    Box {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberCircleIndication(color = colors.interactive),
                    onClick = {
                        onOpen()
                        expanded = true
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Sort, contentDescription = "Ordina per", tint = colors.content)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(colors.bgMainViews)
                // Aggiunge il bordo colorato (spessore 1.dp o quanto preferisci)
                .border(
                    width = 1.dp,
                    color = colors.content,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
        ) {
            SortField.entries.forEach { field ->
                val isSelected = field == current
                DropdownMenuItem(
                    modifier = Modifier
                        .background(if (isSelected) colors.selectedBg else Color.Transparent),
                    text = {
                        Text(
                            text = labels[field] ?: field.name,
                            color = colors.content,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelect(field)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun SortDirectionButton(
    direction: SortDirection,
    onToggle: () -> Unit
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberCircleIndication(color = colors.interactive),
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (direction == SortDirection.ASC) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            contentDescription = if (direction == SortDirection.ASC) "A→Z" else "Z→A",
            tint = colors.content
        )
    }
}