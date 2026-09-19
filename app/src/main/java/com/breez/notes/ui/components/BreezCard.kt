package com.breez.notes.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.breez.notes.ui.theme.NoteCardFillDark
import com.breez.notes.ui.theme.NoteCardFillLight

@Composable
fun BreezCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            elevation = elevation,
            content = content
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            elevation = elevation,
            content = content
        )
    }
}

@Composable
fun noteCardFill(dark: Boolean): Color = if (dark) NoteCardFillDark else NoteCardFillLight
