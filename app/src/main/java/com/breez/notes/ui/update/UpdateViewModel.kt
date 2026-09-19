package com.breez.notes.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breez.notes.domain.model.AppUpdateState
import com.breez.notes.domain.repository.UpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository
) : ViewModel() {

    val uiState: StateFlow<AppUpdateState> = updateRepository.state

    init {
        checkForUpdate()
    }

    fun checkForUpdate() {
        viewModelScope.launch { updateRepository.checkForUpdate() }
    }

    fun startUpdate() {
        updateRepository.startUpdate()
    }

    fun onInstallPermissionResult() {
        updateRepository.onInstallPermissionResult()
    }

    fun consumeInstallPermissionRequest() {
        updateRepository.consumeInstallPermissionRequest()
    }

    fun dismissBanner() {
        updateRepository.dismissBanner()
    }
}
