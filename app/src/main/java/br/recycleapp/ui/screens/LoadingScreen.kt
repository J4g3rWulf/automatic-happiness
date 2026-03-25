package br.recycleapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R
import br.recycleapp.ui.theme.GreenDark
import br.recycleapp.ui.theme.GreenPrimary
import br.recycleapp.ui.theme.RecycleAppTheme
import br.recycleapp.ui.viewmodel.ClassificationViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

/** Tempo mínimo que a tela de loading fica visível — garante UX fluida
 *  mesmo quando a IA termina muito rápido. */
private const val MIN_LOADING_MS = 8000L

/** Intervalo entre cada troca de mensagem (em ms). */
private const val MSG_INTERVAL_MS = 2000L

@Composable
fun LoadingScreen(
    uiState: ClassificationViewModel.UiState,
    onBack: () -> Unit,
    onResult: () -> Unit
) {
    // Marca o momento de entrada para garantir o tempo mínimo de exibição
    val startTime = remember { System.currentTimeMillis() }

    BackHandler { onBack() }

    // Quando a IA terminar, espera o tempo mínimo antes de navegar
    LaunchedEffect(uiState) {
        if (uiState is ClassificationViewModel.UiState.Result) {
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < MIN_LOADING_MS) delay(MIN_LOADING_MS - elapsed)
            onResult()
        }
    }

    // ── Mensagens rotativas — param na última ─────────────────────────
    val messages = listOf(
        R.string.loading_msg_1,
        R.string.loading_msg_2,
        R.string.loading_msg_3,
        R.string.loading_msg_4
    )
    var messageIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (messageIndex < messages.size - 1) {
            delay(MSG_INTERVAL_MS)
            messageIndex++
        }
        // Para na última mensagem ("Quase lá…") e não avança mais
    }

    // ── Animação Lottie ───────────────────────────────────────────────
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.loading_animation)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations  = LottieConstants.IterateForever,
        speed       = 1.5f  // aumente para mais rápido, diminua para mais lento
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary.copy(alpha = 0.21f)),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .offset(y = 24.dp)
        ) {
            val maxW         = maxWidth
            val isNarrow     = maxW < 340.dp
            val textScale    = if (isNarrow) 0.92f else 1f
            val baseStyle    = MaterialTheme.typography.bodyMedium
            val textStyle    = baseStyle.copy(
                fontSize   = (baseStyle.fontSize.value * textScale).sp,
                lineHeight = (baseStyle.lineHeight.value * textScale).sp
            )
            val textMaxWidth = maxW.coerceAtMost(360.dp)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.align(Alignment.Center)
            ) {
                // ── Animação Lottie no lugar do spinner ───────────────
                LottieAnimation(
                    composition = composition,
                    progress    = { progress },
                    modifier    = Modifier.size(180.dp)
                )

                Spacer(Modifier.height(24.dp))

                // ── Texto dinâmico com fade entre mensagens ───────────
                // Troca suavemente entre as mensagens a cada MSG_INTERVAL_MS
                // e para na última ("Quase lá…")
                AnimatedContent(
                    targetState  = messages[messageIndex],
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                    },
                    label = "loading_text"
                ) { msgRes ->
                    Text(
                        text      = stringResource(msgRes),
                        style     = textStyle,
                        color     = GreenDark,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier
                            .padding(horizontal = 8.dp)
                            .widthIn(max = textMaxWidth)
                    )
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(name = "Loading — em andamento", device = Devices.PIXEL_5)
@Composable
private fun LoadingScreenPreview() {
    RecycleAppTheme {
        LoadingScreen(
            uiState  = ClassificationViewModel.UiState.Loading,
            onBack   = {},
            onResult = {}
        )
    }
}