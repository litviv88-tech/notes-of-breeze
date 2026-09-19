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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.breez.notes.R
import com.breez.notes.domain.model.Note
import com.breez.notes.ui.notes.NoteCard
import com.breez.notes.ui.notes.NoteOrganizeAction
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
    swipeLabel: String? = null,
    onToggleChecklistItem: ((Note, Int) -> Unit)? = null,
    onOrganize: ((Note, NoteOrganizeAction) -> Unit)? = null,
    belowCard: (@Composable (Note) -> Unit)? = null
) {
    val view = LocalView.current
    val archiveLabel = swipeLabel ?: stringResource(R.string.note_archive)
    val ordered = remember { mutableStateOf(notes) }
    val dragging = remember { mutableStateOf(false) }
    LaunchedEffect(notes) {
        if (!dragging.value) ordered.value = notes
    }

    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        val fromIndex = ordered.value.indexOfFirst { it.id == from.key }
        val toIndex = ordered.value.indexOfFirst { it.id == to.key }
        if (fromIndex < 0 || toIndex < 0) return@rememberReorderableLazyListState
        ordered.value = ordered.value.moved(fromIndex, toIndex)
    }
    val visibleNotes = ordered.value

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(visibleNotes, key = { it.id }) { note ->
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
                            swipeLabel = archiveLabel,
                            onToggleChecklistItem = onToggleChecklistItem?.let { handler ->
                                { index -> handler(note, index) }
                            },
                            onOrganize = onOrganize?.let { handler ->
                                { action -> handler(note, action) }
                            },
                            reorderHandleModifier = Modifier.longPressDraggableHandle(
                                onDragStarted = {
                                    dragging.value = true
                                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                },
                                onDragStopped = {
                                    dragging.value = false
                                    onReorder(ordered.value)
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
                        onTogglePin = { onTogglePin(note) },
                        swipeLabel = archiveLabel,
                        onToggleChecklistItem = onToggleChecklistItem?.let { handler ->
                            { index -> handler(note, index) }
                        },
                        onOrganize = onOrganize?.let { handler ->
                            { action -> handler(note, action) }
                        }
                    )
                    belowCard?.invoke(note)
                }
            }
        }
    }
}
