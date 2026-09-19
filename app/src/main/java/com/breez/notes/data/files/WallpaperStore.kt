package com.breez.notes.data.files

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun liveFile(): File = File(context.filesDir, "wallpapers/live.mp4").apply {
        parentFile?.mkdirs()
    }

    fun saveLive(source: File): File {
        val dest = liveFile()
        if (source.canonicalPath != dest.canonicalPath) {
            source.copyTo(dest, overwrite = true)
        }
        return dest
    }

    fun deleteLive() {
        liveFile().delete()
    }
}
