package com.example.recycleapp.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onSend: (String) -> Unit
) {
    val ctx = LocalContext.current

    // Logs
    runCatching {
        val f = photoUri.resolveCapturedCacheFile(ctx)
        Log.d("CONFIRM","opened uri=$photoUri | file=${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}")
    }

    BackHandler {
        val f = photoUri.resolveCapturedCacheFile(ctx)
        Log.d("CONFIRM","system back -> delete ${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}")
        photoUri.tryDeleteCapturedCacheFile(context = ctx)
        onBack()
    }

    val buttonWidth = 158.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = {
                        val f = photoUri.resolveCapturedCacheFile(ctx)
                        Log.d("CONFIRM","appbar back -> delete ${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}")
                        photoUri.tryDeleteCapturedCacheFile(ctx)
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFF60AE1D) // GreenPrimary
    ) { inner ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // ===== Responsividade por ALTURA útil da tela =====
            // Em telas pequenas, o botão fica MAIS PERTO da borda inferior (bottomOffset menor).
            val (buttonHeight, buttonBottomOffset, imageOffsetY) = when {
                maxHeight < 620.dp -> Triple(56.dp, 20.dp, (-40).dp)
                maxHeight < 740.dp -> Triple(64.dp, 50.dp, (-64).dp)
                else               -> Triple(72.dp, 104.dp, (-96).dp)
            }

            // Espaço útil para a foto considerando o botão
            val maxImageHeight = maxHeight - (buttonBottomOffset + buttonHeight + 24.dp)

            // Tamanho FIXO desejado
            val desiredW = 320.dp
            val desiredH = 480.dp

            // Garantir que não estoure em telas muito pequenas
            val frameW = minOf(desiredW, maxWidth - 48.dp)      // 24dp de padding em cada lado
            val frameH = minOf(desiredH, maxImageHeight)

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
                    contentScale = ContentScale.Fit, // a imagem se adapta ao retângulo fixo
                    modifier = Modifier.fillMaxSize()
                )
            }

            Button(
                onClick = {
                    val f = photoUri.resolveCapturedCacheFile(ctx)
                    Log.d("CONFIRM","send -> delete ${f?.absolutePath} | exists=${f?.exists()} | len=${f?.length()}")

                    // Navega para a próxima etapa (Loading)
                    onSend(photoUri)

                    // IMPORTANTE: não apagar aqui.
                    // A próxima tela (loading/API) ainda vai precisar desse arquivo.
                    // A limpeza a gente faz na ResultScreen ou quando o usuário voltar.
                    // photoUri.tryDeleteCapturedCacheFile(ctx)
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