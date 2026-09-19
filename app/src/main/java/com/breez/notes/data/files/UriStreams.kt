package com.breez.notes.data.files

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream

fun Context.openUriInput(uri: Uri): InputStream {
    if (uri.scheme == "file") {
        val path = uri.path ?: error("Не удалось прочитать файл")
        return File(path).inputStream()
    }
    return contentResolver.openInputStream(uri) ?: error("Не удалось прочитать файл")
}
