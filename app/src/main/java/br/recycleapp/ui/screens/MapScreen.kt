package br.recycleapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R
import br.recycleapp.domain.map.RecyclingPoint
import br.recycleapp.ui.components.RecycleMapCard
import br.recycleapp.ui.components.RecyclingPointBottomSheet
import br.recycleapp.ui.theme.GreenDark
import br.recycleapp.ui.theme.GreenPrimary

/**
 * Tela dedicada ao mapa de pontos de coleta seletiva.
 *
 * Acessível via aba "Mapa" da bottom nav. Reutiliza [RecycleMapCard] com
 * sizing em tela cheia — o mapa ocupa todo o espaço disponível acima do
 * banner inferior, que exibe o título e uma ilustração decorativa.
 *
 * O filtro de tipos e materiais está disponível via botão flutuante do mapa.
 * O [RecyclingPointBottomSheet] abre ao tocar em qualquer marcador.
 */
@Composable
fun MapScreen() {
    var selectedPoint by remember { mutableStateOf<RecyclingPoint?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Mapa (ocupa todo o espaço restante acima do banner) ──────────
        RecycleMapCard(
            toneColor     = GreenDark,
            modifier      = Modifier
                .fillMaxWidth()
                .weight(1f),
            onMarkerClick = { point -> selectedPoint = point }
        )

        // ── Banner inferior ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenPrimary)
                .padding(horizontal = 24.dp)
                .height(170.dp)
        ) {
            // Texto à esquerda
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.65f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text       = "Encontre no mapa",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    lineHeight = 28.sp
                )
                Text(
                    text       = "locais de descarte",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = "Clique nos pontos para ver informações",
                    fontSize = 12.sp,
                    color    = Color.White.copy(alpha = 0.80f)
                )
            }

            // Ilustração à direita
            // Substitua R.drawable.art_bottom_home pela ilustração correta
            Image(
                painter            = painterResource(R.drawable.art_bottom_home),
                contentDescription = null,
                contentScale       = ContentScale.Fit,
                modifier           = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxHeight()
                    .width(140.dp)
            )
        }
    }

    // ── Bottom sheet de detalhes do ponto ─────────────────────────────────
    selectedPoint?.let { point ->
        RecyclingPointBottomSheet(
            point      = point,
            sheetColor = GreenDark,
            onDismiss  = { selectedPoint = null }
        )
    }
}