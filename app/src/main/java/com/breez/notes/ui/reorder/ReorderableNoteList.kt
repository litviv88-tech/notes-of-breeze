package com.breez.notes.ui.reorder

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.breez.notes.domain.model.Note
import com.breez.notes.ui.notes.NoteCard
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun ReorderableNoteList(
    notes: List<Note>,
    enabled: Boolean,
    onReorder: (List<Note>) -> Unit,
    onOpenNote: (Long) -> Unit,
    onDelete: (Note) -> Unit,
    onTogglePin: (Note) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    belowCard: (@Composable (Note) -> Unit)? = null
) {
    val view = LocalView.current
    var ordered by remember { mutableStateOf(notes) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(notes) {
        if (!dragging) ordered = notes
    }

    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        val fromIndex = ordered.indexOfFirst { it.id == from.key }
        val toIndex = ordered.indexOfFirst { it.id == to.key }
        if (fromIndex < 0 || toIndex < 0) return@rememberReorderableLazyListState
        ordered = ordered.moved(fromIndex, toIndex)
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ordered, key = { it.id }) { note ->
            if (enabled) {
                ReorderableItem(reorderableState, key = note.id) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "noteDrag")
                    Column(
                        modifier = Modifier
                            .zIndex(if (isDragging) 1f else 0f)
                            .shadow(elevation)
                    ) {
                        NoteCard(
                            note = note,
                            onClick = { onOpenNote(note.id) },
                            onDelete = { onDelete(note) },
                            onTogglePin = { onTogglePin(note) },
                            swipeEnabled = !isDragging,
                            reorderHandleModifier = Modifier.longPressDraggableHandle(
                                onDragStarted = {
                                    dragging = true
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                },
                                onDragStopped = {
                                    dragging = false
                                    onReorder(ordered)
                                }
                            )
                        )
                        belowCard?.invoke(note)
                    }
                }
            } else {
                Column {
                    NoteCard(
                        note = note,
                        onClick = { onOpenNote(note.id) },
                        onDelete = { onDelete(note) },
                        onTogglePin = { onTogglePin(note) }
                    )
                    belowCard?.invoke(note)
                }
            }
        }
    }
}
