package com.breez.notes.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.breez.notes.R
import com.breez.notes.domain.model.Folder
import com.breez.notes.ui.components.BreezButton
import com.breez.notes.ui.components.BreezTextButton
import com.breez.notes.ui.components.BreezTextField
import com.breez.notes.ui.folders.FolderMarkBadge

enum class NoteOrganizeAction {
    RENAME,
    COPY,
    MOVE
}

@Composable
fun RenameTitleDialog(
    visible: Boolean,
    title: String,
    initial: String,
    hint: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initial) }
    LaunchedEffect(visible, initial) {
        if (visible) value = initial
    }
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            BreezButton(
                text = stringResource(R.string.folder_save),
                enabled = value.isNotBlank(),
                onClick = { onConfirm(value.trim()) }
            )
        },
        dismissButton = {
            BreezTextButton(text = stringResource(R.string.editor_cancel), onClick = onDismiss)
        },
        title = { Text(title) },
        text = {
            BreezTextField(
                value = value,
                onValueChange = { value = it },
                hint = hint,
                singleLine = true
            )
        }
    )
}

@Composable
fun FolderTargetDialog(
    visible: Boolean,
    title: String,
    folders: List<Folder>,
    selectedId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    var picked by remember { mutableStateOf(selectedId) }
    LaunchedEffect(visible, selectedId) {
        if (visible) picked = selectedId
    }
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            BreezButton(
                text = stringResource(R.string.editor_confirm),
                onClick = { onConfirm(picked) }
            )
        },
        dismissButton = {
            BreezTextButton(text = stringResource(R.string.editor_cancel), onClick = onDismiss)
        },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                FolderTargetRow(
                    name = stringResource(R.string.editor_no_folder),
                    selected = picked == null,
                    onClick = { picked = null }
                )
                folders.forEach { folder ->
                    FolderTargetRow(
                        name = folder.name,
                        selected = picked == folder.id,
                        onClick = { picked = folder.id },
                        folder = folder
                    )
                }
            }
        }
    )
}

@Composable
private fun FolderTargetRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    folder: Folder? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        if (folder != null) {
            FolderMarkBadge(folder = folder, size = 20.dp, modifier = Modifier.padding(end = 8.dp))
        }
        Text(name, style = MaterialTheme.typography.bodyLarge)
    }
}
