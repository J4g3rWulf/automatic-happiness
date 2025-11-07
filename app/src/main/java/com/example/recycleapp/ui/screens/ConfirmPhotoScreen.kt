package com.example.recycleapp.ui.screens

import androidx.core.net.toUri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.recycleapp.R
import com.example.recycleapp.util.tryDeleteCapturedCacheFile
import com.example.recycleapp.util.resolveCapturedCacheFile
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmPhotoScreen(
    photoUri: String,
    onBack: () -> Unit,
    onSend: () -> Unit
) {
    val ctx = LocalContext.current

    BackHandler {
        photoUri.tryDeleteCapturedCacheFile(context = ctx)
        onBack()
    }

    // Log inicial: mostra exatamente qual arquivo em cache estamos tratando
    runCatching {
        val f = photoUri.resolveCapturedCacheFile(ctx)
        Log.d(
            "CONFIRM",
            "Resolveu arquivo: ${f?.absolutePath} exists=${f?.exists()} length=${f?.length()}"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        // Log e deleção ao voltar
                        val toDel = photoUri.resolveCapturedCacheFile(ctx)
                        Log.d(
                            "CONFIRM",
                            "Back: apagar ${toDel?.absolutePath} exists=${toDel?.exists()} len=${toDel?.length()}"
                        )
                        photoUri.tryDeleteCapturedCacheFile(ctx)
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = {
                        onSend() // (futuro upload)
                        // Log e deleção ao enviar
                        val toDel = photoUri.resolveCapturedCacheFile(ctx)
                        Log.d(
                            "CONFIRM",
                            "Send: apagar ${toDel?.absolutePath} exists=${toDel?.exists()} len=${toDel?.length()}"
                        )
                        photoUri.tryDeleteCapturedCacheFile(ctx)
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.send))
                }
            }
        }
    ) { inner ->
        Box(
            Modifier
                .padding(inner)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = photoUri.toUri()),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}