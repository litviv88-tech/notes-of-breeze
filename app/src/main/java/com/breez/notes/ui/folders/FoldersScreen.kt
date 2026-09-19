package com.breez.notes.ui.folders

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.breez.notes.R
import com.breez.notes.ui.components.BreezCard
import com.breez.notes.ui.components.BreezTopBar
import com.breez.notes.ui.reorder.ReorderHandle
import com.breez.notes.ui.reorder.moved
import com.breez.notes.ui.theme.Transparent
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

@Composable
fun FoldersScreen(
    onBack: () -> Unit,
    onOpenFolder: (Long) -> Unit,
    viewModel: FoldersViewModel = hiltViewModel()
) {
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val markError by viewModel.markError.collectAsStateWithLifecycle()
    var dialogOpen by remember { mutableStateOf(false) }
    val view = LocalView.current
    var ordered by remember { mutableStateOf(folders) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(folders) {
        if (!dragging) ordered = folders
    }
    val gridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
        val fromIndex = ordered.indexOfFirst { it.id == from.key }
        val toIndex = ordered.indexOfFirst { it.id == to.key }
        if (fromIndex < 0 || toIndex < 0) return@rememberReorderableLazyGridState
        ordered = ordered.moved(fromIndex, toIndex)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Transparent,
        topBar = {
            BreezTopBar(title = stringResource(R.string.folders_title), onBack = onBack, transparent = true)
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { dialogOpen = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.folder_add))
            }
        }
    ) { padding ->
        if (ordered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.folders_empty),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ordered, key = { it.id }) { folder ->
                    ReorderableItem(reorderableState, key = folder.id) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "folderDrag")
                        Box(
                            modifier = Modifier
                                .zIndex(if (isDragging) 1f else 0f)
                                .shadow(elevation, MaterialTheme.shapes.medium)
                        ) {
                            BreezCard(onClick = { onOpenFolder(folder.id) }) {
                                FolderMarkCover(
                                    folder = folder,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(96.dp)
                                )
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(folder.name, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.folder_notes_count, folder.noteCount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            ReorderHandle(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .longPressDraggableHandle(
                                        onDragStarted = {
                                            dragging = true
                                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                        },
                                        onDragStopped = {
                                            dragging = false
                                            viewModel.reorderFolders(ordered)
                                        }
                                    ),
                                contentDescription = stringResource(R.string.item_reorder)
                            )
                        }
                    }
                }
            }
        }
    }

    FolderEditDialog(
        visible = dialogOpen,
        existing = null,
        error = markError,
        onClearError = viewModel::clearMarkError,
        onDismiss = { dialogOpen = false },
        onSave = { name, colorHex, markType, mediaUri, mimeType ->
            viewModel.createFolder(name, colorHex, markType, mediaUri, mimeType) {
                dialogOpen = false
            }
        }
    )
}
