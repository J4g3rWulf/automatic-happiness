package br.recycleapp.ui.screens

import android.Manifest
import android.content.Context
import androidx.core.net.toUri
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import br.recycleapp.util.resolveCapturedCacheFile
import br.recycleapp.util.tryDeleteCapturedCacheFile
import java.io.File

/**
 * Tela sem UI (headless) — abre a câmera do sistema imediatamente ao entrar.
 *
 * Gerencia o fluxo completo de permissão de câmera e criação de arquivo
 * temporário no cache do app (via FileProvider). Se o usuário capturar a foto,
 * chama [onPhotoTaken] com o URI do arquivo. Se cancelar ou negar a permissão,
 * apaga o arquivo temporário e chama [onBack].
 *
 * @param onBack       chamado ao cancelar ou negar permissão de câmera
 * @param onPhotoTaken chamado com o URI (String) da foto capturada com sucesso
 */
@Composable
fun CameraCaptureScreen(
    onBack: () -> Unit,
    onPhotoTaken: (String) -> Unit
) {
    var pendingUriStr by rememberSaveable { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current

    // helper para abrir a câmera já com um arquivo temporário no cache
    fun launchCamera(context: Context, launcher: ActivityResultLauncher<Uri>) {
        val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
        val image = File.createTempFile("photo_", ".jpg", imagesDir)
        Log.d("CAM", "Temp criado: ${image.absolutePath}")

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            image
        )
        pendingUriStr = uri.toString()
        launcher.launch(uri)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val u = pendingUriStr?.toUri()
        Log.d("CAM", "takePicture success=$success, uri=$u")

        if (success && u != null) {
            val real = u.toString().resolveCapturedCacheFile(ctx)
            Log.d(
                "CAM",
                "realFile=${real?.absolutePath} exists=${real?.exists()} len=${real?.length()}"
            )
            onPhotoTaken(u.toString())
        } else {
            pendingUriStr?.tryDeleteCapturedCacheFile(ctx)
            onBack()
        }
        pendingUriStr = null
    }

    val requestCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera(ctx, takePictureLauncher) else onBack()
    }

    // Abre a câmera imediatamente ao entrar
    LaunchedEffect(Unit) {
        requestCamera.launch(Manifest.permission.CAMERA)
    }

    // Headless: não desenha UI/fundo para evitar "flash" ao retornar
    Box(modifier = Modifier.fillMaxSize()) { /* intencionalmente vazio */ }
}