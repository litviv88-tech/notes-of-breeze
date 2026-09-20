package com.breez.notes.data.files

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoClipper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun durationMs(uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            if (uri.scheme == "file") {
                retriever.setDataSource(uri.path)
            } else {
                retriever.setDataSource(context, uri)
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            runCatching { retriever.release() }
        }
    }

    fun clip(
        source: Uri,
        startMs: Long,
        endMs: Long,
        stripAudio: Boolean = false
    ): File {
        val duration = durationMs(source)
        val start = startMs.coerceAtLeast(0L)
        val rawEnd = if (duration > 0L) endMs.coerceAtMost(duration) else endMs
        val end = rawEnd.coerceAtLeast(start + MIN_CLIP_MS)
        val output = File(context.cacheDir, "clips/${UUID.randomUUID()}.mp4").apply {
            parentFile?.mkdirs()
        }
        val almostFull = start <= 150L && (duration <= 0L || end >= duration - 150L)
        return if (almostFull && !stripAudio) {
            copy(source, output)
            output
        } else {
            runCatching {
                remux(
                    source = source,
                    dest = output,
                    startUs = start * 1_000L,
                    endUs = end * 1_000L,
                    stripAudio = stripAudio
                )
            }.getOrElse { error ->
                output.delete()
                throw error
            }
            output
        }
    }

    private fun copy(source: Uri, dest: File) {
        context.openUriInput(source).use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
    }

    private fun remux(
        source: Uri,
        dest: File,
        startUs: Long,
        endUs: Long,
        stripAudio: Boolean = false
    ) {
        val extractor = MediaExtractor()
        val muxer = MediaMuxer(dest.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        try {
            if (source.scheme == "file") {
                extractor.setDataSource(requireNotNull(source.path))
            } else {
                extractor.setDataSource(context, source, null)
            }
            val indexMap = HashMap<Int, Int>()
            var maxInput = 0
            var rotation = 0
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                val isVideo = mime.startsWith("video/")
                val isAudio = mime.startsWith("audio/")
                if (!isVideo && !(isAudio && !stripAudio)) continue
                extractor.selectTrack(i)
                indexMap[i] = muxer.addTrack(format)
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    maxInput = maxOf(maxInput, format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE))
                }
                if (mime.startsWith("video/")) {
                    rotation = when {
                        format.containsKey(MediaFormat.KEY_ROTATION) ->
                            format.getInteger(MediaFormat.KEY_ROTATION)
                        format.containsKey("rotation-degrees") ->
                            format.getInteger("rotation-degrees")
                        else -> 0
                    }
                }
            }
            if (indexMap.isEmpty()) error("В видео нет дорожек")
            if (rotation != 0) muxer.setOrientationHint(rotation)
            muxer.start()
            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            val buffer = ByteBuffer.allocate(maxOf(maxInput, 1_048_576))
            val info = MediaCodec.BufferInfo()
            var origin = -1L
            while (true) {
                val track = extractor.sampleTrackIndex
                if (track < 0) break
                val time = extractor.sampleTime
                if (time < 0L || time > endUs) break
                val muxIndex = indexMap[track]
                if (muxIndex == null) {
                    extractor.advance()
                    continue
                }
                buffer.clear()
                info.offset = 0
                info.size = extractor.readSampleData(buffer, 0)
                if (info.size < 0) break
                if (origin < 0L) origin = time
                info.presentationTimeUs = (time - origin).coerceAtLeast(0L)
                info.flags = extractor.sampleFlags
                muxer.writeSampleData(muxIndex, buffer, info)
                extractor.advance()
            }
        } finally {
            runCatching { muxer.stop() }
            runCatching { muxer.release() }
            runCatching { extractor.release() }
        }
        if (!dest.exists() || dest.length() < 64L) error("Не удалось обрезать видео")
    }

    companion object {
        const val MIN_CLIP_MS = 400L
    }
}

fun formatVideoTime(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val minutes = totalSec / 60L
    val seconds = totalSec % 60L
    return "%d:%02d".format(minutes, seconds)
}
