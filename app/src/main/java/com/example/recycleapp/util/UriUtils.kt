package com.example.recycleapp.util
import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Apaga o arquivo gerado pela câmera quando ele foi salvo em cacheDir/images
 * e o Uri veio do nosso FileProvider.
 */
fun String.tryDeleteCapturedCacheFile(context: Context) {
    runCatching {
        val u = Uri.parse(this)

        when (u.scheme) {
            "file" -> {
                // Ex.: file:///data/user/0/.../cache/images/photo_123.jpg
                u.path?.let { File(it).delete() }
            }
            "content" -> {
                // Ex.: content://<pkg>.fileprovider/cache/images/photo_123.jpg
                val name = u.lastPathSegment?.substringAfterLast('/') ?: return@runCatching
                val f = File(File(context.cacheDir, "images"), name)
                f.delete()
            }
            else -> Unit
        }
    }
}

// Onde for usar adicione o import -> import com.example.recycleapp.util.tryDeleteIfFilePath
