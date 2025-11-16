package com.example.recycleapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recycleapp.R
import com.example.recycleapp.ui.theme.GreenDark
import com.example.recycleapp.ui.theme.GreenPrimary
import com.example.recycleapp.ui.theme.RedAccent
import com.example.recycleapp.ui.theme.WhiteText
import com.example.recycleapp.ui.theme.RecycleAppTheme
import com.example.recycleapp.util.tryDeleteCapturedCacheFile

// ========= PALETA POR MATERIAL =========

private data class ResultPalette(
    val background: Color,   // fundo da tela
    val tone: Color,         // texto do card, botão "Novo Lixo", borda dos botões do mapa
    val accent: Color,       // detalhes (pin do mapa, etc.)
    @DrawableRes val binIcon: Int
)

private fun paletteForLabel(label: String): ResultPalette {
    return when (label.trim().lowercase()) {
        // Vidro
        "vidro" -> ResultPalette(
            background = Color(0xFF60AE1D),
            tone       = Color(0xFF297B19),
            accent     = Color(0xFF5AAC48),
            binIcon    = R.drawable.ic_green_trash
        )
        // Plástico
        "plástico", "plastico" -> ResultPalette(
            background = Color(0xFFEB555F),
            tone       = Color(0xFFB12B2A),
            accent     = RedAccent,
            binIcon    = R.drawable.ic_red_trash
        )
        // Papel
        "papel" -> ResultPalette(
            background = Color(0xFF3EAFC8),
            tone       = Color(0xFF333AB5),
            accent     = Color(0xFF333AB5),
            binIcon    = R.drawable.ic_blue_trash
        )
        // Metal
        "metal" -> ResultPalette(
            background = Color(0xFFF0C753),
            tone       = Color(0xFFA87B32),
            accent     = Color(0xFFF0C753),
            binIcon    = R.drawable.ic_yellow_trash
        )
        // Fallback
        else -> ResultPalette(
            background = GreenPrimary,
            tone       = GreenDark,
            accent     = GreenDark,
            binIcon    = R.drawable.ic_recycle_loading
        )
    }
}

// ========= TELA DE RESULTADO =========

@Composable
fun ResultScreen(
    photoUri: String,
    label: String,
    onBackToHome: () -> Unit,

    // CONTROLES DO TÍTULO
    titleTopPadding: Dp = 80.dp,
    titleBetweenLines: Dp = 8.dp,
    titleToCardSpacing: Dp = 10.dp,
    headFontSize: Float = 35f,
    labelFontSize: Float = 40f
) {
    val ctx = LocalContext.current
    val palette = remember(label) { paletteForLabel(label) }

    fun clearAndBack() {
        photoUri.tryDeleteCapturedCacheFile(ctx)
        onBackToHome()
    }

    BackHandler { clearAndBack() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ===== TOPO: TÍTULO + CARD + LIXEIRA =====
            Column(modifier = Modifier.fillMaxWidth()) {

                Spacer(modifier = Modifier.height(titleTopPadding))

                // Estilos das duas linhas do título
                val headStyle = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = headFontSize.sp,
                    lineHeight = 34.sp
                )
                val labelStyle = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = labelFontSize.sp,
                    lineHeight = 44.sp
                )

                // Parâmetros da lixeira e da reserva de texto
                val binSize: Dp = 92.dp
                val binOffsetX: Dp = 1.7.dp
                val binOffsetY: Dp = (-40).dp
                val textReserveFactor = 0.6f
                val reserveRightForBin: Dp = binSize * textReserveFactor

                // ---- TÍTULO EM DUAS LINHAS ----
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Linha 1: "O material é" + " . . ."
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 5.dp)
                            .wrapContentWidth(Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.result_head),
                            color = WhiteText,
                            style = headStyle
                        )

                        Spacer(modifier = Modifier.width(19.dp))

                        Text(
                            text = stringResource(R.string.result_head_dots),
                            color = WhiteText,
                            style = headStyle
                        )
                    }

                    Spacer(modifier = Modifier.height(titleBetweenLines))

                    // Linha 2: "Plástico"/"Vidro"/"Papel"/"Metal"
                    val formattedLabel = label.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }

                    val labelPaddingEnd = 16.dp + reserveRightForBin + 30.dp

                    Text(
                        text = formattedLabel,
                        color = WhiteText,
                        style = labelStyle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 16.dp,
                                end = labelPaddingEnd
                            )
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(titleToCardSpacing))

                // ---- CARD + LIXEIRA SOBREPOSTA ----
                Box(modifier = Modifier.fillMaxWidth()) {

                    ResultMapCard(
                        accentColor = palette.accent,
                        toneColor = palette.tone,
                        description = stringResource(R.string.result_dispose_hint),
                        reserveRightForBin = reserveRightForBin
                    )

                    Image(
                        painter = painterResource(id = palette.binIcon),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = binOffsetX, y = binOffsetY)
                            .size(binSize),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // ===== BOTÃO "NOVO LIXO" =====
            Button(
                onClick = { clearAndBack() },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 65.dp)
                    .size(width = 158.dp, height = 64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.tone,
                    contentColor = WhiteText
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                Text(
                    text = stringResource(R.string.result_button_new),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 22.sp,
                        lineHeight = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ========= CARD COM TEXTO + MAPA =========

@Composable
private fun ResultMapCard(
    accentColor: Color,      // cor do pin / detalhes
    toneColor: Color,        // cor principal (texto, botões do mapa)
    description: String,
    reserveRightForBin: Dp
) {
    val cardCorner = 12.dp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cardCorner),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val hintStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.5.sp,
                lineHeight = 19.sp
            )

            val parts = description.split('\n')
            val line1 = parts.getOrNull(0).orEmpty()
            val line2 = parts.getOrNull(1).orEmpty()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = reserveRightForBin)
            ) {
                Text(
                    text = line1,
                    style = hintStyle,
                    color = toneColor,
                    maxLines = 1
                )
                if (line2.isNotEmpty()) {
                    Text(
                        text = line2,
                        style = hintStyle,
                        color = toneColor,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(cardCorner))
                    .background(Color(0xFFEFEFEF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MapNavButton(isLeft = true, toneColor = toneColor)
                    MapNavButton(isLeft = false, toneColor = toneColor)
                }
            }
        }
    }
}

@Composable
private fun MapNavButton(
    isLeft: Boolean,
    toneColor: Color,
    onClick: () -> Unit = {}
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .size(width = 40.dp, height = 32.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, toneColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = WhiteText,
            contentColor = toneColor
        )
    ) {
        val icon =
            if (isLeft) Icons.AutoMirrored.Filled.ArrowBack
            else Icons.AutoMirrored.Filled.ArrowForward

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ================= PREVIEWS (PODEM SER APAGADOS DEPOIS) =================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewResultPlastic() {
    RecycleAppTheme {
        ResultScreen(
            photoUri = "",
            label = "plástico",
            onBackToHome = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewResultGlass() {
    RecycleAppTheme {
        ResultScreen(
            photoUri = "",
            label = "vidro",
            onBackToHome = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewResultPaper() {
    RecycleAppTheme {
        ResultScreen(
            photoUri = "",
            label = "papel",
            onBackToHome = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewResultMetal() {
    RecycleAppTheme {
        ResultScreen(
            photoUri = "",
            label = "metal",
            onBackToHome = {}
        )
    }
}
