package com.breez.notes.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max

@Singleton
class ImageTextRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun fromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val image = runCatching { InputImage.fromFilePath(context, uri) }.getOrNull()
            ?: return@withContext ""
        recognize(image)
    }

    suspend fun fromFile(file: File): String = withContext(Dispatchers.IO) {
        val bitmap = decodeSampled(file) ?: return@withContext ""
        try {
            recognize(InputImage.fromBitmap(bitmap, 0))
        } finally {
            bitmap.recycle()
        }
    }

    private suspend fun recognize(image: InputImage): String {
        return runCatching { recognizer.process(image).await() }.getOrDefault("").trim()
    }

    private fun decodeSampled(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        val largest = max(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
        val options = BitmapFactory.Options().apply {
            inSampleSize = max(1, largest / 1280)
        }
        return BitmapFactory.decodeFile(file.absolutePath, options)
    }

    private suspend fun Task<com.google.mlkit.vision.text.Text>.await(): String =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { continuation.resume(it.text.orEmpty()) }
            addOnFailureListener { continuation.resumeWithException(it) }
        }
}
