package br.recycleapp.ui.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import br.recycleapp.R
import br.recycleapp.domain.map.PointType
import br.recycleapp.domain.map.RecyclingPoint

/**
 * Bottom sheet exibido quando o usuário toca num marcador do mapa.
 *
 * Mostra nome, tipo, endereço e banner de materiais aceitos.
 * Oferece botão para abrir o local no Google Maps nativo.
 *
 * @param point      ponto de coleta selecionado
 * @param sheetColor cor de fundo do sheet — deve corresponder ao material da tela
 * @param onDismiss  callback para fechar o sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclingPointBottomSheet(
    point: RecyclingPoint,
    sheetColor: Color = Color(0xFF1565C0),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor   = sheetColor,
        dragHandle       = {
            Box(
                modifier         = Modifier.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(
                    modifier  = Modifier.width(32.dp),
                    thickness = 4.dp,
                    color     = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {

            // ── Nome do local ─────────────────────────────────────────
            Text(
                text       = point.name,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )

            // ── Subtítulo por tipo ────────────────────────────────────
            Text(
                text     = when (point.type) {
                    PointType.PEV      -> "Ponto de Entrega Voluntária"
                    PointType.ECOPONTO -> "Ecoponto Comlurb"
                },
                fontSize = 13.sp,
                color    = Color.White.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(8.dp))

            // ── Endereço ──────────────────────────────────────────────
            if (point.address.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint               = Color.White.copy(alpha = 0.85f),
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = point.address,
                        fontSize = 13.sp,
                        color    = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
            Spacer(Modifier.height(16.dp))

            // ── Materiais aceitos ─────────────────────────────────────
            Text(
                text       = "Materiais aceitos",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White
            )

            Spacer(Modifier.height(10.dp))

            // ── Banner de materiais ───────────────────────────────────
            Image(
                painter            = painterResource(
                    when (point.type) {
                        PointType.PEV      -> R.drawable.materiais_pev
                        PointType.ECOPONTO -> R.drawable.materiais_eco_pontos
                    }
                ),
                contentDescription = "Materiais aceitos",
                contentScale       = ContentScale.FillWidth,
                modifier           = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // ── Botão abrir no Google Maps ────────────────────────────
            Button(
                onClick = {
                    val uri = "geo:${point.latitude},${point.longitude}?q=${point.latitude},${point.longitude}(${point.name})".toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    runCatching { context.startActivity(intent) }.onFailure {
                        val webUri = "https://www.google.com/maps/search/?api=1&query=${point.latitude},${point.longitude}".toUri()
                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(50.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = sheetColor
                )
            ) {
                Text(
                    text       = "Abrir no Google Maps",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}