package com.breez.notes.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.breez.notes.R
import com.breez.notes.domain.model.ChecklistFormat
import com.breez.notes.domain.model.Note
import com.breez.notes.ui.components.BreezCard
import com.breez.notes.ui.components.noteCardFill
import com.breez.notes.ui.reorder.ReorderHandle
import com.breez.notes.ui.theme.DismissRed
import com.breez.notes.ui.theme.PinGold
import com.breez.notes.ui.theme.PureWhite
import com.breez.notes.ui.theme.parseHexColor

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier,
    swipeEnabled: Boolean = true,
    swipeLabel: String = stringResource(R.string.note_archive),
    onToggleChecklistItem: ((Int) -> Unit)? = null,
    onOrganize: ((NoteOrganizeAction) -> Unit)? = null,
    reorderHandleModifier: Modifier? = null
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = swipeEnabled,
        enableDismissFromEndToStart = swipeEnabled,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DismissRed, MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(text = swipeLabel, color = PureWhite)
            }
        }
    ) {
        BreezCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = noteCardFill(isSystemInDarkTheme()),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(parseHexColor(note.colorHex))
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = note.title.ifBlank { stringResource(R.string.note_untitled) },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.isChecklist) {
                        ChecklistFormat.preview(note.body).forEachIndexed { index, item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (onToggleChecklistItem != null) {
                                    Checkbox(
                                        checked = item.done,
                                        onCheckedChange = { onToggleChecklistItem(index) }
                                    )
                                }
                                Text(
                                    text = if (onToggleChecklistItem == null) {
                                        (if (item.done) "✓  " else "○  ") + item.text
                                    } else {
                                        item.text
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None,
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = if (item.done) 0.45f else 0.75f
                                    )
                                )
                            }
                        }
                    } else if (note.body.isNotBlank()) {
                        Text(
                            text = note.body,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                    if (note.meetingPlace.isNotBlank()) {
                        Text(
                            text = note.meetingPlace,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (note.attachments.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.attachments_count, note.attachments.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                if (!note.isArchived) {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = stringResource(
                                if (note.isPinned) R.string.note_unpin else R.string.note_pin
                            ),
                            tint = if (note.isPinned) PinGold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (onOrganize != null && !note.isArchived) {
                    var menuOpen by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.note_more)
                            )
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.note_rename)) },
                                onClick = {
                                    menuOpen = false
                                    onOrganize(NoteOrganizeAction.RENAME)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.note_copy_to_folder)) },
                                onClick = {
                                    menuOpen = false
                                    onOrganize(NoteOrganizeAction.COPY)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.note_move_to_folder)) },
                                onClick = {
                                    menuOpen = false
                                    onOrganize(NoteOrganizeAction.MOVE)
                                }
                            )
                        }
                    }
                }
                if (reorderHandleModifier != null) {
                    ReorderHandle(
                        modifier = reorderHandleModifier,
                        contentDescription = stringResource(R.string.item_reorder)
                    )
                }
            }
        }
    }
}
