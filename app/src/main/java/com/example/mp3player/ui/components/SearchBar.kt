package com.example.mp3player.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged // <-- IMPORTANTE: Aggiungi questo import
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mp3player.ui.theme.AppTheme

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit, // <-- 1. Aggiungi il parametro nei dati in ingresso
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Bordo: trasparente a riposo, interactive quando ha il focus
    val borderColor = if (isFocused) colors.interactive else Color.Transparent

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { state -> onFocusChanged(state.isFocused) } // <-- 2. Inseriscilo qui nella catena
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            if (isFocused) {
                Text(
                    text = "Cerca brano...",
                    color = colors.content.copy(alpha = 0.4f)
                )
            }
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = colors.content.copy(alpha = 0.5f)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor   = colors.content,
            unfocusedTextColor = colors.content,
            focusedContainerColor   = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            // Nota: Se volevi usare borderColor definito sopra, dovresti passarlo qui sotto
            focusedBorderColor   = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor                = colors.interactive,
            selectionColors            = null,
        )
    )
}
