package com.example.recycleapp.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.recycleapp.util.resolveCapturedCacheFile
import com.example.recycleapp.util.tryDeleteCapturedCacheFile
import java.io.File

@Composable
fun CameraCaptureScreen(
    onBack: () -> Unit,
    onPhotoTaken: (String) -> Unit
) {
    val pendingUri = remember { mutableStateOf<Uri?>(null) }
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
        pendingUri.value = uri
        launcher.launch(uri)
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val u = pendingUri.value
        Log.d("CAM", "takePicture success=$success, uri=$u")

        if (success && u != null) {
            val real = u.toString().resolveCapturedCacheFile(ctx)
            Log.d(
                "CAM",
                "realFile=${real?.absolutePath} exists=${real?.exists()} len=${real?.length()}"
            )
            onPhotoTaken(u.toString())
        } else {
            // Usuário apertou VOLTAR na câmera (ou falhou): limpa e volta pra Home
            u?.toString()?.tryDeleteCapturedCacheFile(ctx)
            onBack()
        }
        pendingUri.value = null
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