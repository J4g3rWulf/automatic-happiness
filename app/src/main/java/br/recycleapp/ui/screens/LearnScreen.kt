package br.recycleapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R
import br.recycleapp.ui.theme.GreenDark
import br.recycleapp.ui.theme.GreenPrimary

/**
 * Tela educativa sobre resíduos recicláveis.
 *
 * Acessível via aba "Aprender" da bottom nav. Apresenta 4 categorias de
 * conteúdo como botões brancos — cada um navegará para uma sub-tela dedicada
 * (implementadas na Etapa 4).
 *
 * Reutiliza o mesmo padrão visual da HomeScreen: fundo verde, arte decorativa
 * no topo e ilustração na base.
 */
@Composable
fun LearnScreen(
    onOpenColors: () -> Unit = {},
    onOpenWhatToDiscard: () -> Unit = {},
    onOpenHowToDiscard: () -> Unit = {},
    onOpenTerms: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary)
    ) {

        // ── Arte decorativa superior ──────────────────────────────────────
        Image(
            painter            = painterResource(R.drawable.learn_art_top),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.TopCenter,
            modifier           = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // ── Arte decorativa inferior ──────────────────────────────────────
        Image(
            painter            = painterResource(R.drawable.learn_art_bottom),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.BottomCenter,
            modifier           = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
        )

        // ── Conteúdo principal ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {

            Spacer(Modifier.height(66.dp))

            // Título
            Text(
                text       = "Aprenda sobre os\nresíduos recicláveis",
                fontSize   = 29.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
                lineHeight = 36.sp,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth(),
                style      = MaterialTheme.typography.headlineLarge
            )

            Spacer(Modifier.height(32.dp))

            // Botões de categoria
            // Botão 1:
            LearnCategoryButton(label = "Cores e significados",   onClick = onOpenColors)
            Spacer(Modifier.height(40.dp))
            // Botão 2:
            LearnCategoryButton(label = "O que descartar ?",      onClick = onOpenWhatToDiscard)
            Spacer(Modifier.height(40.dp))
            // Botão 3:
            LearnCategoryButton(label = "Como descartar ?",       onClick = onOpenHowToDiscard)
            Spacer(Modifier.height(40.dp))
            // Botão 4:
            LearnCategoryButton(label = "Termos e definições",    onClick = onOpenTerms)

            // Espaço para não sobrepor a ilustração
            Spacer(Modifier.height(200.dp))
        }
    }
}

// ── Botão de categoria ────────────────────────────────────────────────────────

@Composable
private fun LearnCategoryButton(
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed         by interactionSource.collectIsPressedAsState()
    val scale             by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = tween(if (isPressed) 80 else 160),
        label         = "learn_btn_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .indication(
                interactionSource = interactionSource,
                indication        = ripple(color = GreenPrimary.copy(alpha = 0.15f))
            )
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        val released = tryAwaitRelease()
                        if (released) {
                            interactionSource.emit(PressInteraction.Release(press))
                            onClick()
                        } else {
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    }
                )
            },
        shape           = RoundedCornerShape(11.dp),
        color           = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = label,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = GreenDark
            )
        }
    }
}