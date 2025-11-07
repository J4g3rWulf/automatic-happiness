package com.example.recycleapp.util
import android.content.Context
import androidx.core.net.toUri
import java.io.File

/** Resolve o arquivo real em cacheDir/images a partir do photoUri usado pelo FileProvider. */
fun String.resolveCapturedCacheFile(context: Context): File? {
    val u = this.toUri()
    return when (u.scheme) {
        "file" -> u.path?.let { File(it) }
        "content" -> {
            // content://<pkg>.fileprovider/images/photo_123.jpg
            val last = u.lastPathSegment ?: return null
            val name = last.substringAfterLast('/') // "photo_123.jpg"
            File(File(context.cacheDir, "images"), name)
        }
        else -> null
    }
}

/** Apaga o arquivo do cache gerado pela câmera (file:// ou content:// do FileProvider). */
fun String.tryDeleteCapturedCacheFile(context: Context) {
    runCatching { resolveCapturedCacheFile(context)?.delete() }
}