package com.breez.notes.domain.model

enum class UpdateStatus {
    Idle,
    Checking,
    UpToDate,
    Available,
    Downloading,
    Error
}

data class AppUpdateState(
    val status: UpdateStatus = UpdateStatus.Idle,
    val currentVersion: String = "",
    val latestVersion: String = "",
    val notes: String = "",
    val progress: Int = 0,
    val error: String? = null,
    val apkUrl: String = "",
    val needsInstallPermission: Boolean = false,
    val bannerDismissed: Boolean = false
) {
    val hasUpdate: Boolean
        get() = status == UpdateStatus.Available ||
            status == UpdateStatus.Downloading ||
            (status == UpdateStatus.Error && latestVersion.isNotBlank())

    val showBanner: Boolean
        get() = hasUpdate && !bannerDismissed
}
