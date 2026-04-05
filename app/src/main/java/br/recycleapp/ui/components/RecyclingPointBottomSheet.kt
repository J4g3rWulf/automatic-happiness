package br.recycleapp.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import br.recycleapp.domain.map.RecyclingPoint

/**
 * Bottom sheet exibido quando o usuário toca num marcador do mapa.
 *
 * Mostra nome, endereço e materiais aceitos no ponto de coleta.
 * Oferece botão para abrir o local no Google Maps nativo.
 *
 * @param point     ponto de coleta selecionado
 * @param onDismiss callback para fechar o sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclingPointBottomSheet(
    point: RecyclingPoint,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor   = MaterialTheme.colorScheme.surface
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
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(4.dp))

            // ── Endereço ──────────────────────────────────────────────
            if (point.address.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text     = point.address,
                        fontSize = 13.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ── Materiais aceitos ─────────────────────────────────────
            Text(
                text       = "Materiais aceitos",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            if (point.materials.isNotEmpty()) {
                point.materials.forEach { material ->
                    Text(
                        text     = "• $material",
                        fontSize = 13.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text     = "Recicláveis em geral — confirme no local",
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
                shape    = RoundedCornerShape(50.dp)
            ) {
                Text("Abrir no Google Maps")
            }
        }
    }
}