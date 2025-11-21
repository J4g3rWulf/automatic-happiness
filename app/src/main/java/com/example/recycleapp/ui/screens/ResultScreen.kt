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

// Dimensões base usadas como referência para a responsividade
private const val BASE_HEIGHT_DP = 800f
private const val BASE_WIDTH_DP = 360f

// Paleta de cores e ícone para cada tipo de material
private data class ResultPalette(
    val background: Color,
    val tone: Color,   // texto do card, botão "Novo Lixo", borda dos botões do mapa
    val accent: Color, // detalhes (pin do mapa, etc.)
    @DrawableRes val binIcon: Int
)

private fun paletteForLabel(label: String): ResultPalette {
    return when (label.trim().lowercase()) {
        "vidro" -> ResultPalette(
            background = Color(0xFF60AE1D),
            tone = Color(0xFF297B19),
            accent = Color(0xFF5AAC48),
            binIcon = R.drawable.ic_green_trashh
        )

        "plástico", "plastico" -> ResultPalette(
            background = Color(0xFFEB555F),
            tone = Color(0xFFB12B2A),
            accent = RedAccent,
            binIcon = R.drawable.ic_red_trashh
        )

        "papel" -> ResultPalette(
            background = Color(0xFF3EAFC8),
            tone = Color(0xFF333AB5),
            accent = Color(0xFF333AB5),
            binIcon = R.drawable.ic_blue_trashh
        )

        "metal" -> ResultPalette(
            background = Color(0xFFF0C753),
            tone = Color(0xFFA87B32),
            accent = Color(0xFFF0C753),
            binIcon = R.drawable.ic_yellow_trashh
        )

        else -> ResultPalette(
            background = GreenPrimary,
            tone = GreenDark,
            accent = GreenDark,
            binIcon = R.drawable.ic_recycle_loading
        )
    }
}

@Composable
fun ResultScreen(
    photoUri: String,
    label: String,
    onBackToHome: () -> Unit,
    // Valores base que podem ser ajustados se o layout de referência mudar
    headFontSize: Float = 40f,
    labelFontSize: Float = 45f,
    titleTopPadding: Dp = 80.dp,
    titleBetweenLines: Dp = 8.dp,
    titleToCardSpacing: Dp = 10.dp,
    cardTextFontSize: Float = 16f,
    cardTextLineHeight: Float = 19f,
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val boxMaxW = maxWidth
            val boxMaxH = maxHeight

            // 1) Escalas relativas às dimensões base
            val rawHScale = boxMaxH.value / BASE_HEIGHT_DP
            val rawWScale = boxMaxW.value / BASE_WIDTH_DP

            val hScale: Float = rawHScale.coerceIn(0.80f, 1.30f)
            val wScale: Float = rawWScale.coerceIn(0.90f, 1.30f)

            val rawUniform = minOf(hScale, wScale)

            // Telas muito próximas da base travam em 1f para evitar microvariações
            val uniformBase: Float =
                if (rawUniform in 0.97f..1.03f) 1f else rawUniform

            val isSmallH = boxMaxH < 700.dp   // phones mais baixos
            val isLargeH = boxMaxH > 900.dp   // phones bem altos

            // Escalas separadas por grupo de elementos
            val scaleForTitle: Float = when {
                isSmallH -> uniformBase * 1.13f
                isLargeH -> uniformBase * 1.03f
                else -> uniformBase
            }

            val scaleForCard: Float = when {
                isSmallH -> uniformBase * 1.13f
                else -> uniformBase
            }

            val scaleForButtons: Float = when {
                isSmallH -> uniformBase * 0.88f
                isLargeH -> uniformBase * 1.04f
                else -> uniformBase
            }

            // 2) Dimensões derivadas das escalas

            // Espaçamentos verticais do título/card
            val titleTopEff: Dp = titleTopPadding * scaleForTitle
            val titleBetweenLinesEff: Dp = titleBetweenLines * scaleForTitle
            val titleToCardEff: Dp = titleToCardSpacing * scaleForTitle

            // Distância do botão para a borda inferior
            val baseButtonBottom = when {
                isSmallH -> 16.dp
                isLargeH -> 32.dp
                else -> 40.dp
            }
            val minBottom = if (isSmallH) 8.dp else 20.dp
            val buttonBottomEff: Dp =
                (baseButtonBottom * scaleForButtons).coerceIn(minBottom, 64.dp)

            // Botão principal
            val buttonWidthBase = 170.dp * scaleForButtons
            val buttonWidth: Dp = buttonWidthBase.coerceIn(150.dp, boxMaxW - 48.dp)
            val buttonHeight: Dp = (64.dp * scaleForButtons).coerceIn(56.dp, 80.dp)

            // Lixeira
            val baseBinSize = when {
                isSmallH -> 90.dp
                isLargeH -> 102.dp
                else -> 97.dp
            }
            val binSize: Dp = (baseBinSize * scaleForCard).coerceIn(70.dp, 112.dp)
            val binOffsetX: Dp = 1.7.dp
            val binOffsetY: Dp = when {
                isSmallH -> (-26).dp
                boxMaxH < 900.dp -> (-36).dp
                else -> (-42).dp
            }

            // Reserva de espaço à direita para não colidir o título com a lixeira
            val reserveRightForBin: Dp = binSize * 0.6f

            // Altura do "mapa"
            val mapHeightFraction = when {
                isSmallH -> 0.40f
                isLargeH -> 0.46f
                else -> 0.43f
            }
            val mapHeight: Dp = (boxMaxH * mapHeightFraction)
                .coerceIn(220.dp, 420.dp)

            // Fontes do título
            val headFontSp = (headFontSize * scaleForTitle).sp
            val labelFontSp = (labelFontSize * scaleForTitle).sp

            // Fontes do card
            val cardFontSp = (cardTextFontSize * scaleForCard).sp
            val cardLineHeightSp = (cardTextLineHeight * scaleForCard).sp

            // Espaço entre "O material é" e ". . ."
            val dotSpacing: Dp = (dotSpacingBase * scaleForTitle)
                .coerceIn(14.dp, 26.dp)

            val headStyle = MaterialTheme.typography.headlineLarge.copy(
                fontSize = headFontSp,
                lineHeight = (34f * scaleForTitle).sp
            )
            val labelStyle = MaterialTheme.typography.headlineLarge.copy(
                fontSize = labelFontSp,
                lineHeight = (44f * scaleForTitle).sp
            )

            // Fonte do texto do botão
            val buttonTextFontSp = (21f * scaleForButtons)
                .coerceIn(18f, 24f)
                .sp

            // Layout principal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Topo: título + card + lixeira
                Column(modifier = Modifier.fillMaxWidth()) {

                    Spacer(modifier = Modifier.height(titleTopEff))

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

                        Spacer(modifier = Modifier.height(titleBetweenLinesEff))

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

                    Box(modifier = Modifier.fillMaxWidth()) {
                        ResultMapCard(
                            accentColor = palette.accent,
                            toneColor = palette.tone,
                            description = stringResource(R.string.result_dispose_hint),
                            reserveRightForBin = reserveRightForBin,
                            mapHeight = mapHeight,
                            hintFontSizeSp = cardFontSp.value,
                            hintLineHeightSp = cardLineHeightSp.value
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

                // Botão "Novo Lixo"
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
                            fontSize = buttonTextFontSp,
                            lineHeight = buttonTextFontSp
                        ),
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

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
        Column(modifier = Modifier.padding(16.dp)) {
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
        modifier = Modifier.size(width = 40.dp, height = 32.dp),
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

// Previews por tipo de material

@Preview(showBackground = true, name = "Resultado - Plástico")
@Composable
private fun ResultScreenPreviewPlastic() {
    RecycleAppTheme {
        ResultScreen(
            photoUri = "",
            label = "Plástico",
            onBackToHome = {}
        )
    }
}

@Preview(showBackground = true, name = "Resultado - Vidro")
@Composable
private fun ResultScreenPreviewGlass() {
    RecycleAppTheme {
        ResultScreen(
            photoUri = "",
            label = "Vidro",
            onBackToHome = {}
        )
    }
}

@Preview(showBackground = true, name = "Resultado - Papel")
@Composable
private fun ResultScreenPreviewPaper() {
    RecycleAppTheme {
        ResultScreen(
            photoUri = "",
            label = "Papel",
            onBackToHome = {}
        )
    }
}

@Preview(showBackground = true, name = "Resultado - Metal")
@Composable
private fun ResultScreenPreviewMetal() {
    RecycleAppTheme {
        ResultScreen(
            photoUri = "",
            label = "Metal",
            onBackToHome = {}
        )
    }
}