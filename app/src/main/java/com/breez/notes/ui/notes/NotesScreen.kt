package com.breez.notes.ui.notes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breez.notes.R
import com.breez.notes.ui.components.BreezTextField
import com.breez.notes.ui.components.BreezTopBar
import com.breez.notes.ui.folders.FolderMarkBadge
import com.breez.notes.ui.reorder.ReorderableNoteList
import com.breez.notes.ui.theme.Transparent

@Composable
fun NotesScreen(
    onOpenNote: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenWallpaper: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val imageSearch = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::searchByImage)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Transparent,
        topBar = {
            BreezTopBar(
                title = stringResource(R.string.notes_title),
                transparent = true,
                actions = {
                    IconButton(onClick = onOpenFolders) {
                        Icon(Icons.Outlined.Folder, contentDescription = stringResource(R.string.folders_title))
                    }
                    IconButton(onClick = onOpenTheme) {
                        Icon(Icons.Outlined.Palette, contentDescription = stringResource(R.string.action_palette))
                    }
                    IconButton(onClick = onOpenWallpaper) {
                        Icon(Icons.Outlined.Image, contentDescription = stringResource(R.string.action_wallpaper))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNote) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.note_add))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BreezTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchChange,
                        modifier = Modifier.weight(1f),
                        hint = stringResource(R.string.notes_search_hint),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            imageSearch.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(
                            Icons.Outlined.ImageSearch,
                            contentDescription = stringResource(R.string.search_by_image)
                        )
                    }
                }
                if (state.searchingImage) {
                    Text(
                        text = stringResource(R.string.search_image_progress),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (!state.searchMessage.isNullOrBlank()) {
                    Text(
                        text = state.searchMessage.orEmpty(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedFolderId == null,
                        onClick = { viewModel.selectFolder(null) },
                        label = { Text(stringResource(R.string.folder_all)) }
                    )
                    state.folders.forEach { folder ->
                        FilterChip(
                            selected = state.selectedFolderId == folder.id,
                            onClick = { viewModel.selectFolder(folder.id) },
                            leadingIcon = {
                                FolderMarkBadge(
                                    folder = folder,
                                    modifier = Modifier.size(22.dp),
                                    size = 22.dp
                                )
                            },
                            label = { Text(folder.name) }
                        )
                    }
                }
                if (state.isEmpty) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.notes_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    ReorderableNoteList(
                        notes = state.notes,
                        enabled = state.searchQuery.isBlank(),
                        onReorder = viewModel::reorderNotes,
                        onOpenNote = onOpenNote,
                        onDelete = viewModel::deleteNote,
                        onTogglePin = viewModel::togglePin
                    )
                }
        }
    }
}
