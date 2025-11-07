package com.example.recycleapp.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.example.recycleapp.R
import com.example.recycleapp.util.tryDeleteCapturedCacheFile
import com.example.recycleapp.util.resolveCapturedCacheFile
import java.io.File
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraCaptureScreen(
    onBack: () -> Unit,
    onPhotoTaken: (String) -> Unit
) {
    val pendingUri = remember { mutableStateOf<Uri?>(null) }
    val ctx = LocalContext.current

    // helper declarado antes de usar (evita forward reference)
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
            // Loga o arquivo REAL do cache (funciona p/ file:// e content://)
            val real = u.toString().resolveCapturedCacheFile(ctx)
            Log.d("CAM", "realFile=${real?.absolutePath} exists=${real?.exists()} len=${real?.length()}")
            onPhotoTaken(u.toString())

        } else {
            // cancelou/falhou: limpa o temporário criado antes do launch
            u?.toString()?.tryDeleteCapturedCacheFile(ctx)
        }
        pendingUri.value = null
    }

    val requestCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera(ctx, takePictureLauncher) else onBack()
    }

    // abre a câmera imediatamente ao entrar
    LaunchedEffect(Unit) {
        requestCamera.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // espaço para preview no futuro
        }
    }
}