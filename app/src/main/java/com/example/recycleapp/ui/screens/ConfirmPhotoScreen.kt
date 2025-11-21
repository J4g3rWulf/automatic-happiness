package com.example.recycleapp.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
    onSend: (String) -> Unit
) {
    val ctx = LocalContext.current

    // Log inicial só pra conferência do arquivo
    runCatching {
        val f = photoUri.resolveCapturedCacheFile(ctx)
        Log.d(
            "CONFIRM",
            "opened uri=$photoUri | file=${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}"
        )
    }

    // Botão físico de voltar: apaga a foto temporária e volta pra tela anterior
    BackHandler {
        val f = photoUri.resolveCapturedCacheFile(ctx)
        Log.d(
            "CONFIRM",
            "system back -> delete ${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}"
        )
        photoUri.tryDeleteCapturedCacheFile(ctx)
        onBack()
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
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Responsividade por altura útil da tela
            val (buttonHeight, buttonBottomOffset, imageOffsetY) = when {
                maxHeight < 620.dp -> Triple(56.dp, 20.dp, (-40).dp)
                maxHeight < 740.dp -> Triple(64.dp, 50.dp, (-64).dp)
                else               -> Triple(72.dp, 104.dp, (-96).dp)
            }

            // Espaço disponível para a foto considerando botão + margem inferior
            val maxImageHeight = maxHeight - (buttonBottomOffset + buttonHeight + 24.dp)

            // Tamanho “ideal” do frame da foto
            val desiredW = 320.dp
            val desiredH = 480.dp

            // Garante que não estoure em telas pequenas
            val frameW = minOf(desiredW, maxWidth - 48.dp)  // 24dp de cada lado
            val frameH = minOf(desiredH, maxImageHeight)

            // Moldura da foto
            Surface(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .size(width = frameW, height = frameH)
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

            // Botão "Enviar"
            Button(
                onClick = {
                    val f = photoUri.resolveCapturedCacheFile(ctx)
                    Log.d(
                        "CONFIRM",
                        "send -> delete ${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}"
                    )

                    // Vai para a próxima etapa (Loading). A foto ainda será usada lá,
                    // por isso NÃO apagamos aqui.
                    onSend(photoUri)
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
                    fontFamily = FontFamily(Font(R.font.poppins_semibold)),
                    color = WhiteText
                )
            }
        }
    }
}