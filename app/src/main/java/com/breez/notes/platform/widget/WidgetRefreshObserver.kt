package com.breez.notes.platform.widget

import android.content.Context
import com.breez.notes.domain.repository.FolderRepository
import com.breez.notes.domain.repository.NoteRepository
import com.breez.notes.domain.repository.WidgetRepository
import com.breez.notes.widget.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRefreshObserver @Inject constructor(
    private val notes: NoteRepository,
    private val folders: FolderRepository,
    private val widgets: WidgetRepository,
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    @OptIn(FlowPreview::class)
    fun start() {
        if (started) return
        started = true
        combine(
            notes.observeAll(),
            folders.observeAll(),
            widgets.observeAll()
        ) { _, _, _ -> }
            .debounce(150)
            .onEach { WidgetUpdater.updateAll(context) }
            .launchIn(scope)
    }
}
