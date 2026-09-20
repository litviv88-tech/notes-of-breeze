package com.breez.notes.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breez.notes.domain.model.Folder
import com.breez.notes.domain.model.Note
import com.breez.notes.domain.model.WidgetConfig
import com.breez.notes.domain.model.WidgetDisplayMode
import com.breez.notes.domain.model.WidgetSourceType
import com.breez.notes.domain.repository.FolderRepository
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class WidgetConfigUiState(
    val config: WidgetConfig = WidgetConfig.default(0),
    val notes: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val previewNotes: List<Note> = emptyList(),
    val isTodoWidget: Boolean = false
)

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val widgetRepository: WidgetRepository,
    noteRepository: NoteRepository,
    folderRepository: FolderRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val configState = MutableStateFlow(WidgetConfig.default(0))
    private val kindState = MutableStateFlow(WidgetKind.NOTES)

    val uiState: StateFlow<WidgetConfigUiState> = combine(
        configState,
        kindState,
        noteRepository.observeAll(),
        folderRepository.observeAll()
    ) { config, kind, notes, folders ->
        val scopedNotes = if (kind == WidgetKind.TODO) {
            notes.filter { it.isChecklist && !it.isArchived }
        } else {
            notes.filter { !it.isArchived }
        }
        val preview = when (config.sourceType) {
            WidgetSourceType.ALL -> scopedNotes
            WidgetSourceType.FOLDER -> scopedNotes.filter { it.folderId == config.folderId }
            WidgetSourceType.NOTE -> scopedNotes.filter { it.id == config.noteId }
        }.take(config.maxNotes.coerceIn(1, if (kind == WidgetKind.TODO) 8 else 5))
        WidgetConfigUiState(
            config = if (kind == WidgetKind.TODO) {
                config.copy(displayMode = WidgetDisplayMode.CHECKLIST)
            } else {
                config
            },
            notes = scopedNotes,
            folders = folders,
            previewNotes = preview,
            isTodoWidget = kind == WidgetKind.TODO
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WidgetConfigUiState())

    fun bind(appWidgetId: Int) {
        viewModelScope.launch {
            val kind = WidgetKinds.of(context, appWidgetId)
            kindState.value = kind
            val existing = widgetRepository.getByAppWidgetId(appWidgetId)
            configState.value = existing ?: defaultWidgetConfig(appWidgetId, kind).copy(
                cellWidth = placedWidth(appWidgetId),
                cellHeight = placedHeight(appWidgetId)
            )
        }
    }

    fun update(block: (WidgetConfig) -> WidgetConfig) {
        configState.update(block)
    }

    fun setSource(type: WidgetSourceType) = update { it.copy(sourceType = type) }
    fun setFolder(id: Long?) = update { it.copy(folderId = id, sourceType = WidgetSourceType.FOLDER) }
    fun setNote(id: Long?) = update { it.copy(noteId = id, sourceType = WidgetSourceType.NOTE) }
    fun setBackground(uri: String?) = update {
        it.copy(
            backgroundUri = uri,
            backgroundScale = 1f,
            backgroundOffsetX = 0f,
            backgroundOffsetY = 0f
        )
    }
    fun setOpacity(value: Float) = update { it.copy(backgroundOpacity = value.coerceIn(0f, 1f)) }
    fun setTextColor(hex: String) = update { it.copy(textColorHex = hex) }
    fun setFontSize(sp: Int) = update { it.copy(fontSizeSp = sp.coerceIn(10, 24)) }
    fun setDisplayMode(mode: WidgetDisplayMode) = update { it.copy(displayMode = mode) }
    fun setMaxNotes(count: Int) = update {
        val max = if (kindState.value == WidgetKind.TODO) 8 else 5
        it.copy(maxNotes = count.coerceIn(1, max))
    }
    fun setCornerRadius(dp: Int) = update { it.copy(cornerRadiusDp = dp.coerceIn(0, 32)) }

    fun setCellWidth(width: Int) = update {
        it.copy(cellWidth = WidgetCells.coerceWidth(width))
    }

    fun setCellHeight(height: Int) = update {
        it.copy(cellHeight = WidgetCells.coerceHeight(height))
    }

    fun setPhotoTransform(scale: Float, offsetX: Float, offsetY: Float) = update {
        it.copy(
            backgroundScale = WidgetImageTransform.coerceScale(scale),
            backgroundOffsetX = offsetX,
            backgroundOffsetY = offsetY
        )
    }

    fun resetPhotoTransform() = update {
        it.copy(backgroundScale = 1f, backgroundOffsetX = 0f, backgroundOffsetY = 0f)
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val kind = kindState.value
            val config = configState.value.let { raw ->
                if (kind == WidgetKind.TODO) {
                    raw.copy(displayMode = WidgetDisplayMode.CHECKLIST)
                } else {
                    raw
                }
            }
            withContext(Dispatchers.IO) {
                if (!config.backgroundUri.isNullOrBlank()) {
                    WidgetBackgroundStore.renderCropped(context, config)
                }
            }
            widgetRepository.upsert(config)
            WidgetUpdater.updateWidget(context, config.appWidgetId)
            onSaved()
        }
    }

    private fun placedWidth(appWidgetId: Int): Int {
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        val dp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        return WidgetCells.fromDp(dp, 0).first.takeIf { dp > 0 } ?: WidgetCells.DEFAULT_WIDTH
    }

    private fun placedHeight(appWidgetId: Int): Int {
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        val dp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        return WidgetCells.fromDp(0, dp).second.takeIf { dp > 0 } ?: WidgetCells.DEFAULT_HEIGHT
    }
}
