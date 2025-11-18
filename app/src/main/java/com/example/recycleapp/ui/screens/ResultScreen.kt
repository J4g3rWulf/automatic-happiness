package com.example.recycleapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.recycleapp.ui.theme.RecycleAppTheme
import com.example.recycleapp.ui.theme.RedAccent
import com.example.recycleapp.ui.theme.WhiteText
import com.example.recycleapp.util.tryDeleteCapturedCacheFile

// ========= PALETA POR MATERIAL =========

private data class ResultPalette(
    val background: Color,
    val tone: Color,     // texto do card, botão "Novo Lixo", borda dos botões do mapa
    val accent: Color,   // detalhes (pin do mapa, etc.)
    @DrawableRes val binIcon: Int
)

private fun paletteForLabel(label: String): ResultPalette {
    return when (label.trim().lowercase()) {
        "vidro" -> ResultPalette(
            background = Color(0xFF60AE1D),
            tone       = Color(0xFF297B19),
            accent     = Color(0xFF5AAC48),
            binIcon    = R.drawable.ic_green_trashh
        )
        "plástico", "plastico" -> ResultPalette(
            background = Color(0xFFEB555F),
            tone       = Color(0xFFB12B2A),
            accent     = RedAccent,
            binIcon    = R.drawable.ic_red_trashh
        )
        "papel" -> ResultPalette(
            background = Color(0xFF3EAFC8),
            tone       = Color(0xFF333AB5),
            accent     = Color(0xFF333AB5),
            binIcon    = R.drawable.ic_blue_trashh
        )
        "metal" -> ResultPalette(
            background = Color(0xFFF0C753),
            tone       = Color(0xFFA87B32),
            accent     = Color(0xFFF0C753),
            binIcon    = R.drawable.ic_yellow_trashh
        )
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

    // Título (valores base; fatores variam por altura)
    headFontSize: Float = 35f,
    labelFontSize: Float = 40f,
    titleTopPadding: Dp = 80.dp,
    titleBetweenLines: Dp = 8.dp,
    titleToCardSpacing: Dp = 10.dp,

    // Texto do card (base)
    cardTextFontSize: Float = 14.5f,
    cardTextLineHeight: Float = 19f,

    // Espaço entre "O material é" e ". . ."
    dotSpacingBase: Dp = 19.dp
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
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val h = maxHeight
            val w = maxWidth

            // Alturas de referência:
            // - smallH: Small Phone
            // - mediumH: Medium Phone / Pixel 8a / Pixel 9
            // - tallH: região dos Pixels grandes
            val smallH  = 640.dp
            val mediumH = 870.dp
            val tallH   = 950.dp

            val isSmall  = h < smallH
            val isMedium = !isSmall && h < mediumH
            // Muito alto + mais largo → Pixel 8 Pro
            val isSuperTallWide = h >= tallH && w >= 440.dp

            // ----- Espaços verticais -----
            val titleTopEff: Dp = when {
                isSmall  -> titleTopPadding * 0.8f
                isMedium -> titleTopPadding * 1.15f
                else     -> titleTopPadding * 1.3f
            }

            val titleToCardEff: Dp = when {
                isSmall  -> titleToCardSpacing * 0.8f
                isMedium -> titleToCardSpacing * 1.1f
                else     -> titleToCardSpacing * 1.3f
            }

            // Botão: quanto maior o paddingBottom, mais pra cima ele fica
            val buttonBottomBase = 65.dp
            val buttonBottomEff: Dp = when {
                isSmall         -> buttonBottomBase * 0.20f
                isMedium        -> buttonBottomBase * 1.30f
                isSuperTallWide -> buttonBottomBase * 1.00f   // 8 Pro
                else            -> buttonBottomBase * 1.20f   // 9 / 9 Pro
            }

            // ----- Lixeira -----
            val binSize: Dp = when {
                isSmall  -> 80.dp
                isMedium -> 92.dp
                else     -> 100.dp
            }
            val binOffsetX: Dp = 1.7.dp
            val binOffsetY: Dp = when {
                isSmall  -> (-32).dp
                isMedium -> (-40).dp
                else     -> (-44).dp
            }

            val reserveRightForBin: Dp = binSize * 0.6f

            // ----- Altura do mapa -----
            val mapMin = 220.dp
            val mapMax = 380.dp

            val mapHeightFraction: Float = when {
                isSmall         -> 0.32f
                isMedium        -> 0.40f   // Medium Phone / 8a / 9
                isSuperTallWide -> 0.42f   // 8 Pro
                else            -> 0.38f   // 9 Pro
            }

            val mapHeight: Dp = (h * mapHeightFraction).coerceIn(mapMin, mapMax)

            // ----- Tamanho do botão -----
            val buttonWidthBase  = 158.dp
            val buttonHeightBase = 64.dp

            val buttonWidth: Dp = when {
                isSmall         -> buttonWidthBase
                isMedium        -> buttonWidthBase
                isSuperTallWide -> buttonWidthBase * 1.15f
                else            -> buttonWidthBase * 1.08f
            }

            val buttonHeight: Dp = when {
                isSmall         -> buttonHeightBase
                isMedium        -> buttonHeightBase
                isSuperTallWide -> buttonHeightBase * 1.12f
                else            -> buttonHeightBase * 1.06f
            }

            // ----- Fontes escaladas -----
            val headFontEff: Float = when {
                isSmall         -> headFontSize * 1.04f
                isMedium        -> headFontSize * 1.20f
                isSuperTallWide -> headFontSize * 1.25f
                else            -> headFontSize * 1.30f
            }

            val labelFontEff: Float = when {
                isSmall  -> labelFontSize * 1.04f
                isMedium -> labelFontSize * 1.15f
                else     -> labelFontSize * 1.30f
            }

            val cardFontEff: Float = when {
                isSmall  -> cardTextFontSize * 1.04f
                isMedium -> cardTextFontSize * 1.20f
                else     -> cardTextFontSize * 1.30f
            }

            val cardLineHeightEff: Float = when {
                isSmall         -> cardTextLineHeight * 0.95f
                isMedium        -> cardTextLineHeight * 1.00f
                isSuperTallWide -> cardTextLineHeight * 1.06f
                else            -> cardTextLineHeight * 1.03f
            }

            // ----- Espaço entre "O material é" e ". . ." -----
            val dotSpacing: Dp = when {
                isSmall         -> dotSpacingBase * 0.9f
                isMedium        -> dotSpacingBase * 1.0f
                isSuperTallWide -> dotSpacingBase * 0.5f
                else            -> dotSpacingBase * 1.4f
            }

            val headStyle = MaterialTheme.typography.headlineLarge.copy(
                fontSize = headFontEff.sp,
                lineHeight = 34.sp
            )
            val labelStyle = MaterialTheme.typography.headlineLarge.copy(
                fontSize = labelFontEff.sp,
                lineHeight = 44.sp
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ===== TOPO: TÍTULO + CARD + LIXEIRA =====
                Column(modifier = Modifier.fillMaxWidth()) {

                    Spacer(modifier = Modifier.height(titleTopEff))

                    // Título em duas linhas
                    Column(modifier = Modifier.fillMaxWidth()) {
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

                            Spacer(modifier = Modifier.width(dotSpacing))

                            Text(
                                text = stringResource(R.string.result_head_dots),
                                color = WhiteText,
                                style = headStyle
                            )
                        }

                        Spacer(modifier = Modifier.height(titleBetweenLines))

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

                    Spacer(modifier = Modifier.height(titleToCardEff))

                    // Card + lixeira sobreposta
                    Box(modifier = Modifier.fillMaxWidth()) {

                        ResultMapCard(
                            accentColor = palette.accent,
                            toneColor = palette.tone,
                            description = stringResource(R.string.result_dispose_hint),
                            reserveRightForBin = reserveRightForBin,
                            mapHeight = mapHeight,
                            hintFontSizeSp = cardFontEff,
                            hintLineHeightSp = cardLineHeightEff
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
                        .padding(bottom = buttonBottomEff)
                        .size(width = buttonWidth, height = buttonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.tone,
                        contentColor = WhiteText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 14.dp
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
}

// ========= CARD COM TEXTO + MAPA =========

@Composable
private fun ResultMapCard(
    accentColor: Color,
    toneColor: Color,
    description: String,
    reserveRightForBin: Dp,
    mapHeight: Dp,
    hintFontSizeSp: Float,
    hintLineHeightSp: Float
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
                fontSize = hintFontSizeSp.sp,
                lineHeight = hintLineHeightSp.sp
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
                    .height(mapHeight)
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

// ================= PREVIEWS =================

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
