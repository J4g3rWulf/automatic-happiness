package br.recycleapp.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
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
import br.recycleapp.ui.theme.GreenDark
import br.recycleapp.ui.theme.GreenPrimary
import br.recycleapp.ui.theme.RecycleAppTheme
import br.recycleapp.ui.theme.WhiteText
import br.recycleapp.util.resolveCapturedCacheFile
import br.recycleapp.util.tryDeleteCapturedCacheFile

/**
 * Tela de confirmação da foto antes de enviar para a IA.
 *
 * Não tem barra de navegação — o usuário volta pelos botões
 * "Tirar outra" / "Escolher outra" na parte inferior.
 *
 * @param retakeLabel texto do botão secundário —
 *   "Tirar outra" para o fluxo de câmera ou "Escolher outra" para galeria.
 * @param retakeButtonWeight proporção do botão esquerdo (0.0 a 1.0)
 * @param sendButtonWeight   proporção do botão direito  (0.0 a 1.0)
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ConfirmPhotoScreen(
    windowSizeClass: WindowSizeClass,
    photoUri: String,
    retakeLabel: String,
    retakeButtonWeight: Float = 0.45f,   // ← proporção do botão esquerdo
    sendButtonWeight: Float   = 0.55f,   // ← proporção do botão direito
    onBack: () -> Unit,
    onSend: (String) -> Unit
) {
    val ctx = LocalContext.current

    runCatching {
        val f = photoUri.resolveCapturedCacheFile(ctx)
        Log.d("CONFIRM", "opened uri=$photoUri | exists=${f?.exists()} | len=${f?.length()}")
    }

    BackHandler {
        photoUri.tryDeleteCapturedCacheFile(ctx)
        onBack()
    }

    // ── Dimensões responsivas por altura da tela ──────────────────────
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
            buttonHeight       = 56.dp
            buttonBottomOffset = 32.dp
            imageOffsetY       = (-40).dp
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
                painter            = painterResource(R.drawable.top_art_2),
                contentDescription = null,
                contentScale       = ContentScale.FillWidth,
                alignment          = Alignment.TopCenter,
                modifier           = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .offset(y = 97.dp)  // ← aumente para descer mais
            )

            // ── Arte decorativa inferior — atrás dos botões ───────────
            Image(
                painter            = painterResource(R.drawable.bottom_art_3),
                contentDescription = null,
                contentScale       = ContentScale.FillWidth,
                alignment          = Alignment.BottomCenter,
                modifier           = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .zIndex(-1f)
            )

            // ── Título centralizado ───────────────────────────────────
            // Usar Text diretamente em vez de TopAppBar elimina o espaço
            // fantasma à esquerda que deslocava o texto visualmente
            Text(
                text      = stringResource(R.string.confirm_title),
                style     = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = FontFamily(Font(R.font.poppins_semibold)),
                    fontSize   = 30.sp
                ),
                color     = WhiteText,
                textAlign = TextAlign.Center,
                modifier  = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()       // respeita a barra de status do sistema
                    .padding(top = 16.dp)      // ← ajuste para mover verticalmente
                    .align(Alignment.TopCenter)
            )

            // ── Foto + botões ─────────────────────────────────────────
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 60.dp)  // ← reserva espaço para o título
            ) {
                val maxImageHeight = maxHeight - (buttonBottomOffset + buttonHeight + 32.dp)
                val frameW         = (maxWidth - 48.dp).coerceAtMost(320.dp)
                val frameH         = maxImageHeight.coerceAtMost(480.dp)

                // Foto com ContentScale.Crop — preenche o frame sem espaço em branco
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .size(width = frameW, height = frameH)
                        .align(Alignment.Center)
                        .offset(y = imageOffsetY),
                    shape  = RoundedCornerShape(16.dp),
                    color  = Color.Transparent,
                    border = BorderStroke(2.dp, WhiteText)  // ← borda branca fina
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
                            .weight(retakeButtonWeight)
                            .height(buttonHeight),
                        shape          = RoundedCornerShape(102.dp),
                        colors         = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF86BF54),
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
                            .weight(sendButtonWeight)
                            .height(buttonHeight),
                        colors         = ButtonDefaults.buttonColors(
                            containerColor = GreenDark,
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