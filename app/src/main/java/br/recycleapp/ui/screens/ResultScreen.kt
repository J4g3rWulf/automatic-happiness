package br.recycleapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R
import br.recycleapp.ui.components.MaterialCard
import br.recycleapp.ui.components.MaterialCardData
import br.recycleapp.ui.components.ResultButton
import br.recycleapp.ui.components.UnknownCard
import br.recycleapp.ui.theme.*
import br.recycleapp.util.tryDeleteCapturedCacheFile

// ── Dados por material ────────────────────────────────────────────────────────

/**
 * Agrupa cores, recursos e dados do card de cada material.
 *
 * @param background cor de fundo principal da tela
 * @param tone       cor usada em textos internos e botão direito
 * @param btnLeft    cor do botão esquerdo "Dicas de descarte"
 * @param btnRight   cor do botão direito "Identifique outro"
 * @param binOffsetX offset horizontal da lixeira sobre o card
 * @param binOffsetY offset vertical da lixeira sobre o card
 * @param binIcon    drawable da lixeira ilustrada
 * @param bgImage    drawable do fundo topográfico
 * @param cardData   dados visuais delegados ao MaterialCard
 */
private data class MaterialData(
    val background: Color,
    val tone: Color,
    val btnLeft: Color,
    val btnRight: Color,
    val binOffsetX: Dp = 0.dp,
    val binOffsetY: Dp = 0.dp,
    @DrawableRes val binIcon: Int,
    @DrawableRes val bgImage: Int,
    val cardData: MaterialCardData
)

/**
 * Retorna o [MaterialData] correspondente ao label classificado pela IA.
 * O `else` cobre "Indefinido", "Desconhecido" e qualquer valor inesperado.
 */
private fun dataForLabel(label: String): MaterialData =
    when (label.trim().lowercase()) {
        "vidro" -> MaterialData(
            background     = GlassBg,
            tone           = GlassTone,
            btnLeft        = GlassBtnLight,
            btnRight       = GlassBtnDark,
            binOffsetX     = 10.dp,
            binOffsetY     = (-49).dp,
            binIcon        = R.drawable.trash_glass,
            bgImage        = R.drawable.bg_green,
            cardData   = MaterialCardData(
                tone           = GlassTone,
                cardTitleColor = GlassCardTitle,
                cardTitle      = R.string.result_glass_title,
                tip1           = R.string.result_glass_tip1,
                tip2           = R.string.result_glass_tip2
            )
        )
        "plástico", "plastico" -> MaterialData(
            background     = PlasticBg,
            tone           = PlasticTone,
            btnLeft        = PlasticBtnLight,
            btnRight       = PlasticBtnDark,
            binOffsetX     = 28.dp,
            binOffsetY     = (-49).dp,
            binIcon        = R.drawable.trash_plastic,
            bgImage        = R.drawable.bg_red,
            cardData   = MaterialCardData(
                tone           = PlasticTone,
                cardTitleColor = PlasticCardTitle,
                cardTitle      = R.string.result_plastic_title,
                tip1           = R.string.result_plastic_tip1,
                tip2           = R.string.result_plastic_tip2
            )
        )
        "papel" -> MaterialData(
            background     = PaperBg,
            tone           = PaperTone,
            btnLeft        = PaperBtnLight,
            btnRight       = PaperBtnDark,
            binOffsetX     = 2.dp,
            binOffsetY     = (-49).dp,
            binIcon        = R.drawable.trash_paper,
            bgImage        = R.drawable.bg_blue,
            cardData   = MaterialCardData(
                tone           = PaperTone,
                cardTitleColor = PaperCardTitle,
                cardTitle      = R.string.result_paper_title,
                tip1           = R.string.result_paper_tip1,
                tip2           = R.string.result_paper_tip2
            )
        )
        "metal" -> MaterialData(
            background     = MetalBg,
            tone           = MetalTone,
            btnLeft        = MetalBtnLight,
            btnRight       = MetalBtnDark,
            binOffsetX     = 22.dp,
            binOffsetY     = (-49).dp,
            binIcon        = R.drawable.trash_metal,
            bgImage        = R.drawable.bg_yellow,
            cardData   = MaterialCardData(
                tone           = MetalTone,
                cardTitleColor = MetalCardTitle,
                cardTitle      = R.string.result_metal_title,
                tip1           = R.string.result_metal_tip1,
                tip2           = R.string.result_metal_tip2
            )
        )
        else -> MaterialData(
            background     = UnknownBg,
            tone           = UnknownTone,
            btnLeft        = UnknownBtnLight,
            btnRight       = UnknownBtnDark,
            binOffsetX     = 27.dp,
            binOffsetY     = (-49).dp,
            binIcon        = R.drawable.trash_unknown,
            bgImage        = R.drawable.bg_grey,
            cardData   = MaterialCardData(
                tone           = UnknownTone,
                cardTitleColor = UnknownCardTitle,
                cardTitle      = R.string.result_unknown_title,
                tip1           = R.string.result_unknown_subtitle,
                tip2           = R.string.result_unknown_subtitle
            )
        )
    }

// ── Tela principal ────────────────────────────────────────────────────────────

/**
 * Tela de resultado da classificação da IA.
 *
 * Exibe o material identificado com fundo topográfico colorido,
 * lixeira ilustrada, card de dicas de descarte e placeholder de mapa.
 * Para o caso "Indefinido", exibe dois cards de orientação.
 *
 * Animações:
 * - [AnimatedVisibility] com fadeIn + slideInVertically — entrada do conteúdo
 */
@Composable
fun ResultScreen(
    photoUri: String,
    label: String,
    onBackToHome: () -> Unit
) {
    val ctx  = LocalContext.current
    val data = remember(label) { dataForLabel(label) }

    // isUnknown controla o layout alternativo da tela Desconhecido
    val isUnknown = label.trim().lowercase().let {
        it == "desconhecido" || it == "indefinido" || it == "unknown"
    }

    fun clearAndBack() {
        photoUri.tryDeleteCapturedCacheFile(ctx)
        onBackToHome()
    }

    BackHandler { clearAndBack() }

    // Dispara a animação de entrada assim que a tela é composta
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Fundo topográfico ─────────────────────────────────────────
        // PNG específico por material - já tem a cor de fundo embutida
        Image(
            painter            = painterResource(data.bgImage),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        // ── Layout principal ──────────────────────────────────────────
        // Box separa o conteúdo superior (ancorado no topo) dos botões
        // (ancorados na base) - assim os botões não se movem quando o
        // conteúdo cresce ou encolhe
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {

            // ── Conteúdo superior - animado ───────────────────────────
            // fadeIn + slideInVertically: aparece com fade enquanto sobe levemente
            AnimatedVisibility(
                visible  = visible,
                enter    = fadeIn(tween(400)) +
                        slideInVertically(tween(400)) { it / 3 },
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Espaço do topo até o subtítulo
                    // isUnknown tem valor menor para compensar o título menor
                    Spacer(Modifier.height(if (isUnknown) 30.dp else 40.dp))

                    // "Material identificado como" — 70% de opacidade
                    Text(
                        text  = stringResource(R.string.result_identified_as),
                        color = WhiteText.copy(alpha = 0.70f),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Espaço entre subtítulo e nome do material
                    Spacer(Modifier.height(if (isUnknown) 15.dp else 4.dp))

                    // Nome do material - fonte grande, bold
                    // isUnknown usa fonte menor pois "Desconhecido" é mais longo
                    Text(
                        text     = label.replaceFirstChar { it.titlecase() },
                        color    = WhiteText,
                        style    = MaterialTheme.typography.headlineLarge.copy(
                            fontSize   = if (isUnknown) 35.sp else 56.sp,  // ← tamanho da fonte
                            //fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // ── Card + lixeira sobrepostos ────────────────────
                    // A lixeira fica na frente do card via offset negativo
                    // que a puxa para cima, sobrepondo a borda superior do card
                    Box(modifier = Modifier.fillMaxWidth()) {

                        if (isUnknown) {
                            UnknownCard(toneColor = data.tone)
                        } else {
                            MaterialCard(data = data.cardData)
                        }

                        // Lixeira — ancoraa no canto superior direito do Box
                        Image(
                            painter            = painterResource(data.binIcon),
                            contentDescription = null,
                            contentScale       = ContentScale.Fit,
                            modifier           = Modifier
                                .size(110.dp)               // ← tamanho da lixeira
                                .offset(
                                    x = data.binOffsetX,    // ← ajuste em dataForLabel()
                                    y = data.binOffsetY     // ← ajuste em dataForLabel()
                                )
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            // ── Botões — ancorados na base ────────────────────────────
            // Independentes do conteúdo acima — não sobem quando o card cresce
            AnimatedVisibility(
                visible  = visible,
                enter    = fadeIn(tween(400, delayMillis = 200)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),  // ← distância do fundo da tela
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botão esquerdo — "Dicas de descarte" (funcionalidade futura)
                    ResultButton(
                        text           = stringResource(R.string.result_btn_tips),
                        onClick        = { /* futuro */ },
                        containerColor = data.btnLeft,
                        modifier       = Modifier.weight(0.45f)
                    )

                    // Botão direito — "Identifique outro" ou "Tente novamente"
                    ResultButton(
                        text           = stringResource(if (isUnknown) R.string.result_btn_retry else R.string.result_btn_identify),
                        onClick        = { clearAndBack() },
                        containerColor = data.btnRight,
                        modifier       = Modifier.weight(0.45f)
                    )
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Resultado - Vidro")
@Composable
private fun ResultScreenPreviewGlass() {
    RecycleAppTheme { ResultScreen(photoUri = "", label = "Vidro", onBackToHome = {}) }
}

@Preview(showBackground = true, name = "Resultado - Plástico")
@Composable
private fun ResultScreenPreviewPlastic() {
    RecycleAppTheme { ResultScreen(photoUri = "", label = "Plástico", onBackToHome = {}) }
}

@Preview(showBackground = true, name = "Resultado - Papel")
@Composable
private fun ResultScreenPreviewPaper() {
    RecycleAppTheme { ResultScreen(photoUri = "", label = "Papel", onBackToHome = {}) }
}

@Preview(showBackground = true, name = "Resultado - Metal")
@Composable
private fun ResultScreenPreviewMetal() {
    RecycleAppTheme { ResultScreen(photoUri = "", label = "Metal", onBackToHome = {}) }
}

@Preview(showBackground = true, name = "Resultado - Desconhecido")
@Composable
private fun ResultScreenPreviewUnknown() {
    RecycleAppTheme { ResultScreen(photoUri = "", label = "Indefinido", onBackToHome = {}) }
}