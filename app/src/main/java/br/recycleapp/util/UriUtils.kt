package br.recycleapp.util

import android.content.Context
import androidx.core.net.toUri
import java.io.File

/**
 * Resolve o arquivo físico em cacheDir/images a partir de uma URI de FileProvider.
 *
 * Suporta URIs com esquema "file://" e "content://" (FileProvider).
 * Útil para limpar arquivos temporários capturados pela câmera.
 *
 * @param context Contexto para acessar cacheDir
 * @return File correspondente ou null se não for possível resolver
 */
fun String.resolveCapturedCacheFile(context: Context): File? {
    val u = this.toUri()
    return when (u.scheme) {
        "file" -> u.path?.let { File(it) }
        "content" -> {
            val last = u.lastPathSegment ?: return null
            val name = last.substringAfterLast('/')
            File(File(context.cacheDir, "images"), name)
        }
        else -> null
    }
}

/**
 * Tenta deletar o arquivo do cache gerado pela câmera.
 *
 * Ignora erros silenciosamente - útil para limpeza de arquivos temporários
 * onde falhas não são críticas.
 *
 * @param context Contexto para acessar cacheDir
 */
fun String.tryDeleteCapturedCacheFile(context: Context) {
    runCatching { resolveCapturedCacheFile(context)?.delete() }
}