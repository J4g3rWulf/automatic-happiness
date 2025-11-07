package com.example.recycleapp.ui.screens

import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.example.recycleapp.R
import com.example.recycleapp.ui.theme.GreenDark
import com.example.recycleapp.ui.theme.WhiteText
import com.example.recycleapp.util.resolveCapturedCacheFile
import com.example.recycleapp.util.tryDeleteCapturedCacheFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmPhotoScreen(
    photoUri: String,
    onBack: () -> Unit,
    onSend: () -> Unit
) {
    val ctx = LocalContext.current

    // ===== Logs de diagnóstico =====
    runCatching {
        val f = photoUri.resolveCapturedCacheFile(ctx)
        Log.d(
            "CONFIRM",
            "opened uri=$photoUri | file=${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}"
        )
    }

    BackHandler {
        val f = photoUri.resolveCapturedCacheFile(ctx)
        Log.d(
            "CONFIRM",
            "system back -> delete ${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}"
        )
        photoUri.tryDeleteCapturedCacheFile(context = ctx)
        onBack()
    }

    // ===== Descobrir proporção real da imagem (sem carregar a imagem toda) =====
    var imageAspect by remember(photoUri) { mutableStateOf<Float?>(null) }

    LaunchedEffect(photoUri) {
        runCatching {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(photoUri.toUri())?.use {
                BitmapFactory.decodeStream(it, null, opts)
            }

            if (opts.outWidth > 0 && opts.outHeight > 0) {
                imageAspect = opts.outHeight.toFloat() / opts.outWidth.toFloat() // H/W
                Log.d("CONFIRM", "calculated aspect = $imageAspect (h/w) from bounds")
            } else {
                Log.d("CONFIRM", "could not read image bounds; fallback aspect will be used")
            }
        }.onFailure { e ->
            Log.w("CONFIRM", "error reading image bounds", e)
        }
    }

    val buttonWidth = 158.dp
    val buttonHeight = 64.dp
    val buttonBottomOffset = 70.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val f = photoUri.resolveCapturedCacheFile(ctx)
                            Log.d(
                                "CONFIRM",
                                "appbar back -> delete ${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}"
                            )
                            photoUri.tryDeleteCapturedCacheFile(ctx)
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFF60AE1D) // GreenPrimary
    ) { inner ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // Altura máxima para a imagem considerando o botão elevado
            val maxImageHeight = maxHeight - (buttonBottomOffset + buttonHeight + 24.dp)
            val imageOffsetY = (-70).dp

            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxImageHeight)
                    .let { base ->
                        val aspect = imageAspect ?: (3f / 4f)
                        base.aspectRatio(aspect, matchHeightConstraintsFirst = false)
                    }
                    .align(Alignment.Center)
                    .offset(y = imageOffsetY),
                shape = RoundedCornerShape(10.dp),
                color = Color.Transparent,
                border = BorderStroke(3.dp, Color.White)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = photoUri.toUri()),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Button(
                onClick = {
                    val f = photoUri.resolveCapturedCacheFile(ctx)
                    Log.d(
                        "CONFIRM",
                        "send -> delete ${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}"
                    )
                    onSend()
                    photoUri.tryDeleteCapturedCacheFile(ctx)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = buttonBottomOffset)
                    .size(width = buttonWidth, height = buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenDark,
                    contentColor = WhiteText
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                Text(
                    text = stringResource(R.string.send),
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily(
                        androidx.compose.ui.text.font.Font(R.font.poppins_semibold)
                    ),
                    color = WhiteText
                )
            }
        }
    }
}