package br.recycleapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
            painter            = painterResource(R.drawable.art_top_v1),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.TopCenter,
            modifier           = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // ── Arte decorativa inferior ──────────────────────────────────────
        Image(
            painter            = painterResource(R.drawable.art_bottom_home),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.BottomCenter,
            modifier           = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
                .offset(y = 24.dp)
        )

        // ── Conteúdo principal ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {

            Spacer(Modifier.height(48.dp))

            // Título
            Text(
                text       = "Aprenda sobre os\nresíduos recicláveis",
                fontSize   = 32.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                lineHeight = 38.sp,
                style      = MaterialTheme.typography.headlineLarge
            )

            Spacer(Modifier.height(40.dp))

            // Botões de categoria
            LearnCategoryButton(label = "Cores e significados",  onClick = onOpenColors)
            Spacer(Modifier.height(16.dp))
            LearnCategoryButton(label = "O que descartar?",       onClick = onOpenWhatToDiscard)
            Spacer(Modifier.height(16.dp))
            LearnCategoryButton(label = "Como descartar?",        onClick = onOpenHowToDiscard)
            Spacer(Modifier.height(16.dp))
            LearnCategoryButton(label = "Termos e definições",   onClick = onOpenTerms)

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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = ripple(color = GreenPrimary.copy(alpha = 0.15f)),
                onClick           = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = label,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = GreenDark
            )
        }
    }
}