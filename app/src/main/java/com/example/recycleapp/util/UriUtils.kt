package com.example.recycleapp.util
import android.content.Context
import androidx.core.net.toUri
import java.io.File

/**
 * Apaga o arquivo gerado pela câmera quando foi salvo em cacheDir/images
 * (via FileProvider). Funciona para Uris "file://" e "content://<pkg>.fileprovider".
 */
fun String.tryDeleteCapturedCacheFile(context: Context) {
    runCatching {
        val u = this.toUri()
        when (u.scheme) {
            "file" -> {
                // Ex.: file:///data/user/0/.../cache/images/photo_123.jpg
                u.path?.let { File(it).delete() }
            }
            "content" -> {
                // Ex.: content://<pkg>.fileprovider/cache/images/photo_123.jpg
                val last = u.lastPathSegment ?: return@runCatching
                val name = last.substringAfterLast('/') // "photo_123.jpg"
                File(File(context.cacheDir, "images"), name).delete()
            }
            else -> Unit
        }
    }
}