package com.breez.notes.ui.notes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breez.notes.R
import com.breez.notes.domain.model.AppUpdateState
import com.breez.notes.domain.model.ThemeMode
import com.breez.notes.ui.components.BreezTextField
import com.breez.notes.ui.components.BreezTopBar
import com.breez.notes.ui.folders.FolderEditDialog
import com.breez.notes.ui.folders.FolderMarkBadge
import com.breez.notes.ui.folders.FoldersViewModel
import com.breez.notes.ui.reorder.ReorderableNoteList
import com.breez.notes.ui.settings.ThemeViewModel
import com.breez.notes.ui.settings.UpdateSettingsBlock
import com.breez.notes.ui.theme.Transparent

@Composable
fun NotesScreen(
    onOpenNote: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onCreateTodo: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenWallpaper: () -> Unit,
    updateState: AppUpdateState = AppUpdateState(),
    onCheckUpdate: () -> Unit = {},
    onStartUpdate: () -> Unit = {},
    viewModel: NotesViewModel = hiltViewModel(),
    foldersViewModel: FoldersViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val themeSettings by themeViewModel.settings.collectAsStateWithLifecycle()
    val markError by foldersViewModel.markError.collectAsStateWithLifecycle()
    var folderDialog by remember { mutableStateOf(false) }
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
                    IconButton(onClick = onCreateNote) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.note_add))
                    }
                }
            )
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
            Box(modifier = Modifier.weight(1f)) {
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
                        onTogglePin = viewModel::togglePin,
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 8.dp)
                    )
                }
            }
            ListItem(
                headlineContent = { Text(stringResource(R.string.todo_create)) },
                supportingContent = { Text(stringResource(R.string.todo_create_subtitle)) },
                leadingContent = { Icon(Icons.Outlined.CheckBox, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Transparent),
                modifier = Modifier.clickable(onClick = onCreateTodo)
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.folder_create_home)) },
                supportingContent = { Text(stringResource(R.string.folder_create_home_subtitle)) },
                leadingContent = { Icon(Icons.Outlined.CreateNewFolder, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Transparent),
                modifier = Modifier.clickable {
                    folderDialog = true
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_appearance)) },
                supportingContent = { Text(stringResource(R.string.settings_appearance_subtitle)) },
                leadingContent = { Icon(Icons.Outlined.Palette, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Transparent),
                modifier = Modifier.clickable(onClick = onOpenTheme)
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_wallpaper)) },
                supportingContent = { Text(stringResource(R.string.settings_wallpaper_subtitle)) },
                leadingContent = { Icon(Icons.Outlined.Image, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Transparent),
                modifier = Modifier.clickable(onClick = onOpenWallpaper)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = themeSettings.themeMode == ThemeMode.LIGHT,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.LIGHT) },
                    leadingIcon = { Icon(Icons.Outlined.LightMode, contentDescription = null) },
                    label = { Text(stringResource(R.string.theme_mode_light)) }
                )
                FilterChip(
                    selected = themeSettings.themeMode == ThemeMode.DARK,
                    onClick = { themeViewModel.setThemeMode(ThemeMode.DARK) },
                    leadingIcon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                    label = { Text(stringResource(R.string.theme_mode_dark)) }
                )
            }
            UpdateSettingsBlock(
                state = updateState,
                onCheckUpdate = onCheckUpdate,
                onStartUpdate = onStartUpdate,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
    FolderEditDialog(
        visible = folderDialog,
        existing = null,
        error = markError,
        onClearError = foldersViewModel::clearMarkError,
        onDismiss = { folderDialog = false },
        onSave = { name, colorHex, markType, mediaUri, mimeType ->
            foldersViewModel.createFolder(name, colorHex, markType, mediaUri, mimeType) {
                folderDialog = false
            }
        }
    )
}
