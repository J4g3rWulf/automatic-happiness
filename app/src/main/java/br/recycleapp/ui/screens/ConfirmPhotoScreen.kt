package br.recycleapp.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import br.recycleapp.R
import br.recycleapp.ui.theme.GreenDark
import br.recycleapp.ui.theme.RecycleAppTheme
import br.recycleapp.ui.theme.WhiteText
import br.recycleapp.util.resolveCapturedCacheFile
import br.recycleapp.util.tryDeleteCapturedCacheFile

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ConfirmPhotoScreen(
    windowSizeClass: WindowSizeClass,
    photoUri: String,
    onBack: () -> Unit,
    onSend: (String) -> Unit
) {
    val ctx = LocalContext.current

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
        photoUri.tryDeleteCapturedCacheFile(ctx)
        onBack()
    }

    // Substitui os breakpoints manuais de altura pelo WindowHeightSizeClass
    val buttonHeight: Dp
    val buttonBottomOffset: Dp
    val imageOffsetY: Dp

    when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Compact -> {
            buttonHeight       = 56.dp
            buttonBottomOffset = 20.dp
            imageOffsetY       = (-40).dp
        }
        WindowHeightSizeClass.Medium -> {
            buttonHeight       = 64.dp
            buttonBottomOffset = 50.dp
            imageOffsetY       = (-64).dp
        }
        else -> {
            buttonHeight       = 72.dp
            buttonBottomOffset = 104.dp
            imageOffsetY       = (-96).dp
        }
    }

    val buttonWidth = 158.dp

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
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint               = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color(0xFF60AE1D)
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val maxImageHeight = maxHeight - (buttonBottomOffset + buttonHeight + 24.dp)

            val desiredW = 320.dp
            val desiredH = 480.dp

            val frameW = minOf(desiredW, maxWidth - 48.dp)
            val frameH = minOf(desiredH, maxImageHeight)

            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .size(width = frameW, height = frameH)
                    .align(Alignment.Center)
                    .offset(y = imageOffsetY),
                shape  = RoundedCornerShape(10.dp),
                color  = Color.Transparent,
                border = BorderStroke(3.dp, Color.White)
            ) {
                Image(
                    painter            = rememberAsyncImagePainter(model = photoUri.toUri()),
                    contentDescription = null,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier.fillMaxSize()
                )
            }

            Button(
                onClick = {
                    val f = photoUri.resolveCapturedCacheFile(ctx)
                    Log.d(
                        "CONFIRM",
                        "send -> delete ${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}"
                    )
                    onSend(photoUri)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = buttonBottomOffset)
                    .size(width = buttonWidth, height = buttonHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenDark,
                    contentColor   = WhiteText
                ),
                shape     = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                Text(
                    text       = stringResource(R.string.send),
                    fontSize   = 22.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily(Font(R.font.poppins_semibold)),
                    color      = WhiteText
                )
            }
        }
    }
}

// ===== Previews =====

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Confirm — Pixel 5", device = Devices.PIXEL_5)
@Composable
private fun ConfirmPhotoScreenPreview() {
    RecycleAppTheme {
        ConfirmPhotoScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                androidx.compose.ui.unit.DpSize(360.dp, 780.dp)
            ),
            photoUri = "",
            onBack   = {},
            onSend   = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Confirm — Compacto altura", widthDp = 360, heightDp = 500)
@Composable
private fun ConfirmPhotoScreenPreviewCompact() {
    RecycleAppTheme {
        ConfirmPhotoScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                androidx.compose.ui.unit.DpSize(360.dp, 500.dp)
            ),
            photoUri = "",
            onBack   = {},
            onSend   = {}
        )
    }
}