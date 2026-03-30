package br.recycleapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R
import br.recycleapp.ui.theme.RecycleAppTheme
import br.recycleapp.ui.theme.WhiteText
import br.recycleapp.util.tryDeleteCapturedCacheFile

// ── Dados por material ────────────────────────────────────────────────────────

/**
 * Agrupa todas as cores, recursos e strings de cada material.
 * Adicione novos campos aqui se precisar de mais customizações por material.
 *
 * @param background    cor de fundo principal da tela
 * @param tone          cor usada em textos internos e botão direito
 * @param cardTitleColor cor dos títulos dentro do card branco
 * @param btnLeft       cor do botão esquerdo "Dicas de descarte"
 * @param btnRight      cor do botão direito "Identifique outro"
 * @param binOffsetX    offset horizontal da lixeira sobre o card — ajuste no dataForLabel()
 * @param binOffsetY    offset vertical da lixeira sobre o card — ajuste no dataForLabel()
 * @param binIcon       drawable da lixeira ilustrada
 * @param bgImage       drawable do fundo topográfico
 * @param cardTitle     string res do título do card (ex: "Descarte na lixeira verde!")
 * @param tip1          string res da primeira dica de descarte
 * @param tip2          string res da segunda dica de descarte
 */
private data class MaterialData(
    val background: Color,
    val tone: Color,
    val cardTitleColor: Color,
    val btnLeft: Color,
    val btnRight: Color,
    val binOffsetX: Dp = 0.dp,
    val binOffsetY: Dp = 0.dp,
    @DrawableRes val binIcon: Int,
    @DrawableRes val bgImage: Int,
    val cardTitle: Int,
    val tip1: Int,
    val tip2: Int
)

/**
 * Retorna o [MaterialData] correspondente ao label classificado pela IA.
 * O `else` cobre "Indefinido", "Desconhecido" e qualquer valor inesperado.
 */
private fun dataForLabel(label: String): MaterialData =
    when (label.trim().lowercase()) {
        "vidro" -> MaterialData(
            background     = Color(0xFF3DAF3F),
            tone           = Color(0xFF1E6B20),
            cardTitleColor = Color(0xFF297B19),
            btnLeft        = Color(0xFF86BF54),
            btnRight       = Color(0xFF1B6216),
            binOffsetX     = 10.dp,   // ← ajuste horizontal da lixeira
            binOffsetY     = (-49).dp,// ← ajuste vertical da lixeira
            binIcon        = R.drawable.trash_glass,
            bgImage        = R.drawable.bg_green,
            cardTitle      = R.string.result_glass_title,
            tip1           = R.string.result_glass_tip1,
            tip2           = R.string.result_glass_tip2
        )
        "plástico", "plastico" -> MaterialData(
            background     = Color(0xFFCC3333),
            tone           = Color(0xFF8B1A1A),
            cardTitleColor = Color(0xFF962E2E),
            btnLeft        = Color(0xFFBC5353),
            btnRight       = Color(0xFF621616),
            binOffsetX     = 28.dp,
            binOffsetY     = (-49).dp,
            binIcon        = R.drawable.trash_plastic,
            bgImage        = R.drawable.bg_red,
            cardTitle      = R.string.result_plastic_title,
            tip1           = R.string.result_plastic_tip1,
            tip2           = R.string.result_plastic_tip2
        )
        "papel" -> MaterialData(
            background     = Color(0xFF3A9FCC),
            tone           = Color(0xFF1A5F8B),
            cardTitleColor = Color(0xFF161C62),
            btnLeft        = Color(0xFF549BCD),
            btnRight       = Color(0xFF161C62),
            binOffsetX     = 2.dp,
            binOffsetY     = (-49).dp,
            binIcon        = R.drawable.trash_paper,
            bgImage        = R.drawable.bg_blue,
            cardTitle      = R.string.result_paper_title,
            tip1           = R.string.result_paper_tip1,
            tip2           = R.string.result_paper_tip2
        )
        "metal" -> MaterialData(
            background     = Color(0xFFD4A820),
            tone           = Color(0xFF8B6A00),
            cardTitleColor = Color(0xFF60581E),
            btnLeft        = Color(0xFFD0B761),
            btnRight       = Color(0xFF625916),
            binOffsetX     = 22.dp,
            binOffsetY     = (-49).dp,
            binIcon        = R.drawable.trash_metal,
            bgImage        = R.drawable.bg_yellow,
            cardTitle      = R.string.result_metal_title,
            tip1           = R.string.result_metal_tip1,
            tip2           = R.string.result_metal_tip2
        )
        else -> MaterialData(
            // Cobre "Indefinido", "Desconhecido" e qualquer label inesperado
            background     = Color(0xFF9E9E9E),
            tone           = Color(0xFF424242),
            cardTitleColor = Color(0xFF5C5C5C),
            btnLeft        = Color(0xFFB7B7B7),
            btnRight       = Color(0xFF5C5C5C),
            binOffsetX     = 27.dp,
            binOffsetY     = (-49).dp,
            binIcon        = R.drawable.trash_unknown,
            bgImage        = R.drawable.bg_grey,
            cardTitle      = R.string.result_unknown_title,
            tip1           = R.string.result_unknown_subtitle,
            tip2           = R.string.result_unknown_subtitle
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
        // PNG específico por material — já tem a cor de fundo embutida
        Image(
            painter            = painterResource(data.bgImage),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        // ── Layout principal ──────────────────────────────────────────
        // Box separa o conteúdo superior (ancorado no topo) dos botões
        // (ancorados na base) — assim os botões não se movem quando o
        // conteúdo cresce ou encolhe
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {

            // ── Conteúdo superior — animado ───────────────────────────
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

                    // Nome do material — fonte grande, bold
                    // isUnknown usa fonte menor pois "Desconhecido" é mais longo
                    Text(
                        text     = label.replaceFirstChar { it.titlecase() },
                        color    = WhiteText,
                        style    = MaterialTheme.typography.headlineLarge.copy(
                            fontSize   = if (isUnknown) 35.sp else 56.sp,  // ← tamanho da fonte
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.poppins_semibold))
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
                            MaterialCard(data = data, toneColor = data.tone)
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
                    Button(
                        onClick        = { /* futuro */ },
                        modifier       = Modifier
                            .weight(0.45f)
                            .height(56.dp),
                        shape          = RoundedCornerShape(102.dp),
                        colors         = ButtonDefaults.buttonColors(
                            containerColor = data.btnLeft,
                            contentColor   = WhiteText
                        ),
                        elevation      = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text       = stringResource(R.string.result_btn_tips),
                            fontSize   = 13.sp,
                            maxLines   = 1,
                            fontFamily = FontFamily(Font(R.font.poppins_semibold)),
                            color      = WhiteText,
                            textAlign  = TextAlign.Center
                        )
                    }

                    // Botão direito — "Identifique outro" ou "Tente novamente"
                    Button(
                        onClick        = { clearAndBack() },
                        modifier       = Modifier
                            .weight(0.45f)
                            .height(56.dp),
                        shape          = RoundedCornerShape(102.dp),
                        colors         = ButtonDefaults.buttonColors(
                            containerColor = data.btnRight,
                            contentColor   = WhiteText
                        ),
                        elevation      = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text       = stringResource(
                                if (isUnknown) R.string.result_btn_retry
                                else           R.string.result_btn_identify
                            ),
                            fontSize   = 13.sp,
                            maxLines   = 1,
                            fontFamily = FontFamily(Font(R.font.poppins_semibold)),
                            color      = WhiteText,
                            textAlign  = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ── Componentes privados ──────────────────────────────────────────────────────

/**
 * Card branco com título bold, dois bullet points de dicas
 * e placeholder de mapa para materiais identificados.
 */
@Composable
private fun MaterialCard(
    data: MaterialData,
    toneColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = Color.White
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 9.dp)) {

            // Título "Descarte na lixeira (cor)!"
            Text(
                text  = stringResource(data.cardTitle),
                color = data.cardTitleColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp  // ← tamanho do título do card
                )
            )

            Spacer(Modifier.height(2.dp))

            // Dicas de descarte com bullet points
            BulletText(text = stringResource(data.tip1), color = toneColor, fontSize = 13.sp, startPadding = 8.dp)
            Spacer(Modifier.height(2.dp))
            BulletText(text = stringResource(data.tip2), color = toneColor, fontSize = 13.sp, startPadding = 8.dp)

            Spacer(Modifier.height(14.dp))

            // Título da seção de mapa
            Text(
                text  = stringResource(R.string.result_map_title),
                color = data.cardTitleColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily(Font(R.font.poppins_bold)),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp  // ← tamanho do título do mapa
                )
            )

            Spacer(Modifier.height(10.dp))

            // Placeholder do mapa — será substituído pela integração real futuramente
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(300.dp)  // ← altura do placeholder do mapa
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFEFEF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.Place,
                    contentDescription = null,
                    tint               = toneColor,
                    modifier           = Modifier.size(32.dp)
                )
            }
        }
    }
}

/**
 * Dois cards empilhados para o caso "Indefinido/Desconhecido":
 * - Card 1: explica que não foi possível identificar e sugere nova foto
 * - Card 2: orienta sobre símbolos de reciclagem (implementação futura)
 */
@Composable
private fun UnknownCard(toneColor: Color) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)  // ← espaço entre os dois cards
    ) {
        // Card 1 — explicação do erro
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            color    = Color.White
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 9.dp)) {
                Text(
                    text  = stringResource(R.string.result_unknown_title),
                    color = toneColor,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.poppins_bold)),
                        fontSize   = 16.sp
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = stringResource(R.string.result_unknown_subtitle),
                    color = Color(0xFF555555),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                )
            }
        }

        // Card 2 — símbolos de reciclagem (implementação futura)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            color    = Color.White
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 9.dp)) {
                Text(
                    text  = stringResource(R.string.result_unknown_card2_title),
                    color = toneColor,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.poppins_bold)),
                        fontSize   = 15.sp
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = stringResource(R.string.result_unknown_card2_subtitle),
                    color = Color(0xFF555555),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                )
                Spacer(Modifier.height(20.dp))

                // Placeholder do carrossel de símbolos — alinhado à esquerda
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(190.dp)  // ← altura do placeholder
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF333333)),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text      = stringResource(R.string.result_unknown_placeholder),
                        color     = Color.White,
                        fontSize  = 11.sp,
                        textAlign = TextAlign.Start,
                        modifier  = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Linha de texto com bullet point "• " à esquerda.
 * Usado nas dicas de descarte dentro do [MaterialCard].
 *
 * @param startPadding recuo à esquerda para indentação visual
 */
@Composable
private fun BulletText(
    text: String,
    color: Color,
    fontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    startPadding: Dp = 0.dp
) {
    Row(
        modifier          = Modifier.padding(start = startPadding),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "• ", color = color, fontSize = fontSize)
        Text(text = text,  color = color, fontSize = fontSize)
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