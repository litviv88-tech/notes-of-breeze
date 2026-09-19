package com.breez.notes.domain.repository

import com.breez.notes.domain.model.AppUpdateState
import kotlinx.coroutines.flow.StateFlow

interface UpdateRepository {
    val state: StateFlow<AppUpdateState>
    suspend fun checkForUpdate()
    fun startUpdate()
    fun onInstallPermissionResult()
    fun consumeInstallPermissionRequest()
    fun dismissBanner()
}
