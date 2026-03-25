package br.recycleapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class HomeScreenDimensions(
    // Escalas
    val scaleForTitle: Float,
    val scaleForButtons: Float,

    // Espaçamentos verticais
    val titleTopEff: Dp,
    val warningTopEff: Dp,

    // Botões
    val cardSize: Dp,
    val pairWidth: Dp,
    val leftInset: Dp,

    // Texto do aviso
    val noticeTextScale: Float,

    // Arte inferior
    val contentBottomPadding: Dp
)

@Composable
fun rememberHomeScreenDimensions(
    boxMaxW: Dp,
    boxMaxH: Dp,
    wScale: Float,
    titleTop: Dp,
    buttonTargetSize: Dp,
    buttonGap: Dp,
    warningTop: Dp,
    bottomGuardFactor: Float,
    aspectRatio: Float
): HomeScreenDimensions {
    return remember(boxMaxW, boxMaxH, wScale) {
        val bpSmallH = 700.dp
        val bpTinyH  = 630.dp
        val hScale: Float = when {
            boxMaxH < bpTinyH  -> 0.80f
            boxMaxH < bpSmallH -> 0.90f
            else               -> 1.00f
        }
        val isSmallH = hScale < 1f

        val scaleForButtons = hScale * wScale
        val scaleForTitle   = if (isSmallH) 0.95f else wScale

        val titleTopEff   = titleTop * hScale
        val warningTopEff = warningTop

        val effectiveTarget = buttonTargetSize * scaleForButtons
        val cardSize: Dp    = ((boxMaxW - buttonGap) / 2).coerceAtMost(effectiveTarget)
        val pairWidth       = cardSize * 2 + buttonGap
        val leftInset       = (boxMaxW - pairWidth) / 2

        val noticeTextScale = when {
            pairWidth < 280.dp -> 0.86f
            pairWidth < 320.dp -> 0.92f
            else               -> 1.00f
        }

        val illusHeight = boxMaxW / aspectRatio
        val guardFactor = when {
            hScale <= 0.80f -> bottomGuardFactor * 0.55f
            hScale <  1.00f -> bottomGuardFactor * 0.70f
            else            -> bottomGuardFactor
        }
        val contentBottomPadding = illusHeight * guardFactor

        HomeScreenDimensions(
            scaleForTitle        = scaleForTitle,
            scaleForButtons      = scaleForButtons,
            titleTopEff          = titleTopEff,
            warningTopEff        = warningTopEff,
            cardSize             = cardSize,
            pairWidth            = pairWidth,
            leftInset            = leftInset,
            noticeTextScale      = noticeTextScale,
            contentBottomPadding = contentBottomPadding
        )
    }
}