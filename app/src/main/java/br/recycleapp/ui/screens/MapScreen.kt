package br.recycleapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R
import br.recycleapp.domain.map.RecyclingPoint
import br.recycleapp.ui.components.map.RecycleMapCard
import br.recycleapp.ui.components.map.RecyclingPointBottomSheet
import br.recycleapp.ui.theme.GreenPrimary
import br.recycleapp.ui.theme.RecycleAppTheme

@Composable
fun MapScreen() {
    var selectedPoint by remember { mutableStateOf<RecyclingPoint?>(null) }

    // O Box externo serve apenas para deixar a Menina flutuar sobre a tela toda
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Camada de Fundo: Empilhamento Estrito ─────────────
        Column(modifier = Modifier.fillMaxSize()) {

            // 1. O Mapa
            // O weight(1f) garante que o mapa pare EXATAMENTE onde o bloco de baixo começa.
            RecycleMapCard(
                toneColor     = GreenPrimary,
                modifier      = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onMarkerClick = { point -> selectedPoint = point }
            )

            // 2. O Bloco do Banner (Linha + Fundo Verde)
            // Agrupados linha e banner para a sombra aplicar neles juntos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            ) {
                // A Linha Branca
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color.White.copy(alpha = 0.8f))
                )

                // O Banner Verde
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(123.dp)
                        .background(GreenPrimary)
                ) {
                    // Imagem como fundo — atrás de tudo
                    Image(
                        painter            = painterResource(R.drawable.map_art_bottom_1),
                        contentDescription = null,
                        contentScale       = ContentScale.FillBounds,
                        modifier           = Modifier.fillMaxSize()
                    )

                    // Texto na frente da imagem
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth(0.70f)
                            .padding(start = 24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text       = "Encontre no mapa",
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White,
                            lineHeight = 26.sp
                        )
                        Text(
                            text       = "locais de descarte",
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = Color.White,
                            lineHeight = 26.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text       = "Clique nos pontos para ver mais",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color      = Color.White.copy(alpha = 0.80f),
                            modifier   = Modifier.offset(x = 2.dp)
                        )
                    }
                }
            }
        }

        // ── Camada da Frente: A Menina (Flutuando sobre tudo) ───────────
        Image(
            painter            = painterResource(R.drawable.map_art_bottom_2),
            contentDescription = null,
            contentScale       = ContentScale.Fit,
            modifier = Modifier
                .requiredHeight(160.dp)
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp)
        )
    }

    // ── Bottom sheet do ponto selecionado ────────────────────────────────
    selectedPoint?.let { point ->
        RecyclingPointBottomSheet(
            point      = point,
            sheetColor = GreenPrimary,
            onDismiss  = { selectedPoint = null }
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@PreviewScreenSizes
@Composable
private fun MapScreenPreview() {
    RecycleAppTheme {
        MapScreen()
    }
}