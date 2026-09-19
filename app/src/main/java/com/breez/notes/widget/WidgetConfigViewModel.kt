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
    val previewNotes: List<Note> = emptyList()
)

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val widgetRepository: WidgetRepository,
    noteRepository: NoteRepository,
    folderRepository: FolderRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val configState = MutableStateFlow(WidgetConfig.default(0))

    val uiState: StateFlow<WidgetConfigUiState> = combine(
        configState,
        noteRepository.observeAll(),
        folderRepository.observeAll()
    ) { config, notes, folders ->
        val preview = when (config.sourceType) {
            WidgetSourceType.ALL -> notes
            WidgetSourceType.FOLDER -> notes.filter { it.folderId == config.folderId }
            WidgetSourceType.NOTE -> notes.filter { it.id == config.noteId }
        }.take(config.maxNotes.coerceIn(1, 5))
        WidgetConfigUiState(
            config = config,
            notes = notes,
            folders = folders,
            previewNotes = preview
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WidgetConfigUiState())

    fun bind(appWidgetId: Int) {
        viewModelScope.launch {
            val existing = widgetRepository.getByAppWidgetId(appWidgetId)
            configState.value = existing ?: WidgetConfig.default(appWidgetId).copy(
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
    fun setMaxNotes(count: Int) = update { it.copy(maxNotes = count.coerceIn(1, 5)) }
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
            val config = configState.value
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
