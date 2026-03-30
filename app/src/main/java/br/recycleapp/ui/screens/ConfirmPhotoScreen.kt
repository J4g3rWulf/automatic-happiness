package br.recycleapp.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import br.recycleapp.R
import br.recycleapp.ui.theme.*
import br.recycleapp.util.resolveCapturedCacheFile
import br.recycleapp.util.tryDeleteCapturedCacheFile

/**
 * Tela de confirmação da foto antes de enviar para a IA.
 *
 * Não tem barra de navegação — o usuário volta pelos botões
 * "Tirar outra" / "Escolher outra" na parte inferior.
 *
 * @param retakeLabel        texto do botão esquerdo — vindo do AppNav
 * @param retakeButtonWeight proporção do botão esquerdo (0.0 a 1.0)
 * @param sendButtonWeight   proporção do botão direito  (0.0 a 1.0)
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ConfirmPhotoScreen(
    windowSizeClass: WindowSizeClass,
    photoUri: String,
    retakeLabel: String,
    retakeButtonWeight: Float = 0.45f,
    sendButtonWeight: Float   = 0.55f,
    onBack: () -> Unit,
    onSend: (String) -> Unit
) {
    val ctx = LocalContext.current

    runCatching {
        val f = photoUri.resolveCapturedCacheFile(ctx)
        Log.d("CONFIRM", "opened uri=$photoUri | exists=${f?.exists()} | len=${f?.length()}")
    }

    // Ao pressionar o botão físico de voltar, deleta o arquivo temporário
    BackHandler {
        photoUri.tryDeleteCapturedCacheFile(ctx)
        onBack()
    }

    // ── Dimensões responsivas por altura da tela ──────────────────────
    // Compact: telas baixas (ex: landscape) | Medium: intermediário | else: normal
    val buttonHeight: Dp
    val buttonBottomOffset: Dp
    val imageOffsetY: Dp

    when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Compact -> {
            buttonHeight       = 52.dp
            buttonBottomOffset = 16.dp
            imageOffsetY       = (-32).dp
        }
        WindowHeightSizeClass.Medium -> {
            buttonHeight       = 60.dp
            buttonBottomOffset = 40.dp
            imageOffsetY       = (-56).dp
        }
        else -> {
            buttonHeight       = 56.dp   // altura dos botões
            buttonBottomOffset = 32.dp   // distância dos botões até o fundo
            imageOffsetY       = (-40).dp // posição vertical da foto
        }
    }

    Scaffold(containerColor = GreenPrimary) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // ── Arte decorativa superior ──────────────────────────────
            Image(
                painter            = painterResource(R.drawable.art_top_v2),
                contentDescription = null,
                contentScale       = ContentScale.FillWidth,
                alignment          = Alignment.TopCenter,
                modifier           = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .offset(y = 97.dp)  // ← aumente para descer a arte
            )

            // ── Arte decorativa inferior — fica atrás dos botões ──────
            // zIndex(-1f) garante que a arte fique atrás dos botões
            Image(
                painter            = painterResource(R.drawable.art_bottom_v3),
                contentDescription = null,
                contentScale       = ContentScale.FillWidth,
                alignment          = Alignment.BottomCenter,
                modifier           = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .zIndex(-1f)
            )

            // ── Título centralizado ───────────────────────────────────
            // Text direto (sem TopAppBar) evita o espaço fantasma
            // à esquerda que deslocava o texto visualmente
            Text(
                text      = stringResource(R.string.confirm_title),
                style     = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily(Font(R.font.poppins_semibold)),
                    fontSize   = 30.sp   // ← tamanho da fonte do título
                ),
                color     = WhiteText,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 16.dp)  // ← mova verticalmente aqui
                    .align(Alignment.TopCenter)
            )

            // ── Foto + botões ─────────────────────────────────────────
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 60.dp)  // ← reserva espaço para o título
            ) {
                // Calcula o tamanho máximo da foto respeitando o espaço dos botões
                val maxImageHeight = maxHeight - (buttonBottomOffset + buttonHeight + 32.dp)
                val frameW         = (maxWidth - 48.dp).coerceAtMost(320.dp)
                val frameH         = maxImageHeight.coerceAtMost(480.dp)

                // Foto com borda branca e ContentScale.Crop (sem espaço em branco)
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .size(width = frameW, height = frameH)
                        .align(Alignment.Center)
                        .offset(y = imageOffsetY),
                    shape  = RoundedCornerShape(16.dp),
                    color  = Color.Transparent,
                    border = BorderStroke(2.dp, WhiteText)
                ) {
                    Image(
                        painter            = rememberAsyncImagePainter(model = photoUri.toUri()),
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                }

                // ── Dois botões lado a lado ───────────────────────────
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = buttonBottomOffset),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Botão esquerdo — "Tirar outra" ou "Escolher outra"
                    Button(
                        onClick = {
                            photoUri.tryDeleteCapturedCacheFile(ctx)
                            onBack()
                        },
                        modifier       = Modifier
                            .weight(retakeButtonWeight)  // ← proporção definida no AppNav
                            .height(buttonHeight),
                        shape          = RoundedCornerShape(102.dp),
                        colors         = ButtonDefaults.buttonColors(
                            containerColor = GlassBtnLight,
                            contentColor   = WhiteText
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        elevation      = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text       = retakeLabel,
                            fontSize   = 14.sp,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            fontFamily = FontFamily(Font(R.font.poppins_semibold)),
                            color      = WhiteText
                        )
                    }

                    // Botão direito — "Enviar para análise"
                    Button(
                        onClick        = { onSend(photoUri) },
                        modifier       = Modifier
                            .weight(sendButtonWeight)  // ← proporção definida no AppNav
                            .height(buttonHeight),
                        colors         = ButtonDefaults.buttonColors(
                            containerColor = GlassBtnDark ,
                            contentColor   = WhiteText
                        ),
                        shape          = RoundedCornerShape(102.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        elevation      = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        )
                    ) {
                        Text(
                            text       = stringResource(R.string.confirm_send),
                            fontSize   = 14.sp,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                            fontFamily = FontFamily(Font(R.font.poppins_semibold)),
                            color      = WhiteText
                        )
                    }
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Confirm — câmera — Pixel 5", device = Devices.PIXEL_5)
@Composable
private fun ConfirmPhotoScreenPreviewCamera() {
    RecycleAppTheme {
        ConfirmPhotoScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                androidx.compose.ui.unit.DpSize(360.dp, 780.dp)
            ),
            photoUri    = "",
            retakeLabel = "Tirar outra",
            onBack      = {},
            onSend      = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Confirm — galeria — Pixel 5", device = Devices.PIXEL_5)
@Composable
private fun ConfirmPhotoScreenPreviewGallery() {
    RecycleAppTheme {
        ConfirmPhotoScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                androidx.compose.ui.unit.DpSize(360.dp, 780.dp)
            ),
            photoUri    = "",
            retakeLabel = "Escolher outra",
            onBack      = {},
            onSend      = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Confirm — altura compacta", widthDp = 360, heightDp = 500)
@Composable
private fun ConfirmPhotoScreenPreviewCompact() {
    RecycleAppTheme {
        ConfirmPhotoScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                androidx.compose.ui.unit.DpSize(360.dp, 500.dp)
            ),
            photoUri    = "",
            retakeLabel = "Tirar outra",
            onBack      = {},
            onSend      = {}
        )
    }
}