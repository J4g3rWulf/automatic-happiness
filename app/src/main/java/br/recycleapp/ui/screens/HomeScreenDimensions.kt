package br.recycleapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Agrupa todas as dimensões calculadas da HomeScreen.
 * Calculadas uma vez em [rememberHomeScreenDimensions] e
 * reutilizadas ao longo do layout - evita recalcular a cada recomposição.
 */
data class HomeScreenDimensions(

    // ── Escalas de fonte e ícone ──────────────────────────────────────
    /** Escala aplicada ao título (reduzida em telas baixas). */
    val scaleForTitle: Float,
    /** Escala aplicada aos botões e ícones (combina altura e largura). */
    val scaleForButtons: Float,

    // ── Espaçamentos verticais derivados da altura da tela ────────────
    /** Distância do topo até o título, já ajustada pela altura da tela. */
    val titleTopEff: Dp,

    // ── Dimensões dos botões ──────────────────────────────────────────
    /** Tamanho de cada card quadrado (câmera / galeria). */
    val cardSize: Dp,
    /** Largura total dos dois cards + gap entre eles. */
    val pairWidth: Dp,
    /** Recuo esquerdo para centralizar o par de botões na tela. */
    val leftInset: Dp,

    // ── Texto do card de dica ─────────────────────────────────────────
    /** Fator de escala da fonte do aviso — reduzido em telas estreitas. */
    val noticeTextScale: Float
)

/**
 * Calcula e memoriza as dimensões da HomeScreen com base no
 * tamanho disponível da janela ([boxMaxW] x [boxMaxH]) e na
 * escala de largura ([wScale]) fornecida pelo WindowSizeClass.
 *
 * O [remember] só recalcula quando alguma das chaves mudar —
 * ou seja, quando o tamanho da tela ou a orientação mudar.
 */
@Composable
fun rememberHomeScreenDimensions(
    boxMaxW: Dp,
    boxMaxH: Dp,
    wScale: Float,
    titleTop: Dp,
    buttonTargetSize: Dp,
    buttonGap: Dp
): HomeScreenDimensions {
    return remember(boxMaxW, boxMaxH, wScale, titleTop, buttonTargetSize, buttonGap) {

        // ── Escala vertical baseada na altura disponível ──────────────
        // Telas muito baixas recebem escala reduzida para não cortar conteúdo
        val hScale: Float = when {
            boxMaxH < 630.dp -> 0.80f   // telas muito baixas (ex: dobradiça fechada)
            boxMaxH < 700.dp -> 0.90f   // telas moderadamente baixas
            else             -> 1.00f   // altura normal — sem redução
        }
        val isSmallH = hScale < 1f

        // ── Escalas separadas por grupo de elementos ──────────────────
        // O título reduz menos que os botões em telas pequenas
        val scaleForButtons = hScale * wScale
        val scaleForTitle   = if (isSmallH) 0.95f else wScale

        // ── Espaçamento do topo até o título ──────────────────────────
        val titleTopEff = titleTop * hScale

        // ── Dimensões dos botões ──────────────────────────────────────
        // O card nunca ultrapassa o alvo definido, mas pode ser menor
        // em telas estreitas para não vazar da tela
        val effectiveTarget = buttonTargetSize * scaleForButtons
        val cardSize: Dp    = ((boxMaxW - buttonGap) / 2).coerceAtMost(effectiveTarget)
        val pairWidth       = cardSize * 2 + buttonGap
        val leftInset       = (boxMaxW - pairWidth) / 2

        // ── Escala da fonte do aviso ──────────────────────────────────
        // Reduz a fonte em telas estreitas para o texto caber em 2 linhas
        val noticeTextScale = when {
            pairWidth < 280.dp -> 0.86f
            pairWidth < 320.dp -> 0.92f
            else               -> 1.00f
        }

        HomeScreenDimensions(
            scaleForTitle   = scaleForTitle,
            scaleForButtons = scaleForButtons,
            titleTopEff     = titleTopEff,
            cardSize        = cardSize,
            pairWidth       = pairWidth,
            leftInset       = leftInset,
            noticeTextScale = noticeTextScale
        )
    }
}