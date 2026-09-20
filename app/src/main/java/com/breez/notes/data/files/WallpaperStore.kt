package com.breez.notes.data.files

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoClipper: VideoClipper
) {
    fun liveFile(): File = File(context.filesDir, "wallpapers/live.mp4").apply {
        parentFile?.mkdirs()
    }

    fun saveLive(source: File): File {
        val dest = liveFile()
        val silent = videoClipper.clip(
            source = Uri.fromFile(source),
            startMs = 0L,
            endMs = Long.MAX_VALUE / 2,
            stripAudio = true
        )
        try {
            if (silent.canonicalPath != dest.canonicalPath) {
                silent.copyTo(dest, overwrite = true)
            }
        } finally {
            if (silent.canonicalPath != dest.canonicalPath) {
                silent.delete()
            }
        }
        return dest
    }

    fun deleteLive() {
        liveFile().delete()
    }
}
