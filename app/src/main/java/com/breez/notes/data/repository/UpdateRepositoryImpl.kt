package com.breez.notes.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.breez.notes.domain.model.AppUpdateState
import com.breez.notes.domain.model.UpdateStatus
import com.breez.notes.domain.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UpdateRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(
        AppUpdateState(currentVersion = installedVersionName())
    )
    override val state: StateFlow<AppUpdateState> = _state

    override suspend fun checkForUpdate() {
        if (_state.value.status == UpdateStatus.Downloading) return
        _state.update {
            it.copy(
                status = UpdateStatus.Checking,
                error = null,
                currentVersion = installedVersionName()
            )
        }
        try {
            val remote = withContext(Dispatchers.IO) { fetchManifest() }
            val localCode = installedVersionCode()
            if (remote.versionCode > localCode) {
                _state.update {
                    it.copy(
                        status = UpdateStatus.Available,
                        latestVersion = remote.versionName,
                        notes = remote.notes,
                        apkUrl = remote.apkUrl,
                        error = null
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        status = UpdateStatus.UpToDate,
                        latestVersion = remote.versionName.ifBlank { it.currentVersion },
                        notes = "",
                        apkUrl = remote.apkUrl,
                        error = null
                    )
                }
            }
        } catch (error: Exception) {
            _state.update {
                it.copy(
                    status = UpdateStatus.Error,
                    error = error.message ?: "update_failed"
                )
            }
        }
    }

    override fun startUpdate() {
        if (needsUnknownSourcesPermission()) {
            _state.update { it.copy(needsInstallPermission = true) }
            return
        }
        downloadAndInstall()
    }

    override fun onInstallPermissionResult() {
        _state.update { it.copy(needsInstallPermission = false) }
        if (!needsUnknownSourcesPermission()) {
            downloadAndInstall()
        }
    }

    override fun consumeInstallPermissionRequest() {
        _state.update { it.copy(needsInstallPermission = false) }
    }

    override fun dismissBanner() {
        _state.update { it.copy(bannerDismissed = true) }
    }

    private fun downloadAndInstall() {
        val apkUrl = _state.value.apkUrl
        if (apkUrl.isBlank()) return
        scope.launch {
            _state.update {
                it.copy(
                    status = UpdateStatus.Downloading,
                    progress = 0,
                    error = null
                )
            }
            try {
                val file = downloadApk(apkUrl) { progress ->
                    _state.update { it.copy(progress = progress) }
                }
                installApk(file)
                _state.update { it.copy(status = UpdateStatus.Available, progress = 100) }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        status = UpdateStatus.Error,
                        error = error.message ?: "update_failed"
                    )
                }
            }
        }
    }

    private fun fetchManifest(): RemoteManifest {
        val url = URL("$MANIFEST_URL?t=${System.currentTimeMillis()}")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 12_000
            instanceFollowRedirects = true
            setRequestProperty("Cache-Control", "no-cache")
            setRequestProperty("Accept", "application/json")
        }
        connection.inputStream.bufferedReader().use { reader ->
            val json = JSONObject(reader.readText())
            return RemoteManifest(
                versionCode = json.optInt("versionCode"),
                versionName = json.optString("versionName"),
                apkUrl = json.optString("apkUrl").ifBlank { DEFAULT_APK_URL },
                notes = json.optString("notes")
            )
        }
    }

    private fun downloadApk(apkUrl: String, onProgress: (Int) -> Unit): File {
        val connection = (URL(apkUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 60_000
            instanceFollowRedirects = true
        }
        val length = connection.contentLengthLong
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val file = File(dir, "BreezNotes.apk")
        connection.inputStream.use { input ->
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var downloaded = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    downloaded += read
                    if (length > 0) {
                        onProgress(((downloaded * 100) / length).toInt().coerceIn(0, 100))
                    }
                }
            }
        }
        if (file.length() == 0L) {
            error("empty_apk")
        }
        return file
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }

    private fun needsUnknownSourcesPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
    }

    private fun installedVersionName(): String {
        return runCatching { packageInfo().versionName }.getOrNull().orEmpty().ifBlank { "1.0" }
    }

    private fun installedVersionCode(): Long {
        val info = packageInfo()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }

    private fun packageInfo() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.PackageInfoFlags.of(0)
        )
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }

    private data class RemoteManifest(
        val versionCode: Int,
        val versionName: String,
        val apkUrl: String,
        val notes: String
    )

    private companion object {
        const val MANIFEST_URL =
            "https://litviv88-tech.github.io/notes-of-breeze/version.json"
        const val DEFAULT_APK_URL =
            "https://litviv88-tech.github.io/notes-of-breeze/downloads/BreezNotes.apk"
    }
}
