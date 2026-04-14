package br.recycleapp.ui.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import br.recycleapp.domain.map.RecyclingPoint
import br.recycleapp.ui.mapper.MaterialDrawableMapper

/**
 * Bottom sheet exibido quando o usuário toca num marcador do mapa.
 *
 * Layout dinâmico: cada linha de informação (subtítulo, endereço, horário, benefício)
 * é exibida apenas quando o campo correspondente não está vazio. Não sobra espaço vazio
 * quando um campo está ausente no Firestore.
 *
 * Os materiais aceitos são exibidos em carrossel horizontal com imagem individual
 * por material, mapeada via [MaterialDrawableMapper].
 *
 * @param point      ponto de coleta selecionado
 * @param sheetColor cor de fundo do sheet — vem do material classificado na tela de resultado
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

            // ── Nome do local ─────────────────────────────────────────────────
            Text(
                text       = point.name,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )

            // ── Subtítulo — campo livre por pin, oculto se vazio ──────────────
            if (point.subtitle.isNotEmpty()) {
                Text(
                    text     = point.subtitle,
                    fontSize = 13.sp,
                    color    = Color.White.copy(alpha = 0.75f)
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Endereço — oculto se vazio ────────────────────────────────────
            if (point.address.isNotEmpty()) {
                InfoRow(icon = Icons.Filled.LocationOn, text = point.address)
                Spacer(Modifier.height(6.dp))
            }

            // ── Horário de funcionamento — oculto se vazio ────────────────────
            if (point.schedule.isNotEmpty()) {
                InfoRow(icon = Icons.Filled.Schedule, text = point.schedule)
                Spacer(Modifier.height(6.dp))
            }

            // ── Benefício — oculto se vazio ───────────────────────────────────
            if (point.benefit.isNotEmpty()) {
                InfoRow(icon = Icons.Filled.Autorenew, text = point.benefit)
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
            Spacer(Modifier.height(16.dp))

            // ── Materiais aceitos — carrossel dinâmico ────────────────────────
            if (point.materials.isNotEmpty()) {
                Text(
                    text       = "Materiais aceitos",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )

                Spacer(Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding        = PaddingValues(end = 4.dp)
                ) {
                    items(point.materials) { material ->
                        MaterialChipCard(material = material)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // ── Botão abrir no Google Maps ─────────────────────────────────────
            Button(
                onClick = {
                    val uri = ("geo:${point.latitude},${point.longitude}" +
                            "?q=${point.latitude},${point.longitude}(${point.name})").toUri()
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    runCatching { context.startActivity(intent) }.onFailure {
                        val webUri = ("https://www.google.com/maps/search/" +
                                "?api=1&query=${point.latitude},${point.longitude}").toUri()
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

// ── Componentes privados ──────────────────────────────────────────────────────

/**
 * Linha de informação com ícone alinhado ao topo do texto.
 * Funciona corretamente mesmo quando o texto quebra em múltiplas linhas.
 */
@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = Color.White.copy(alpha = 0.85f),
            modifier           = Modifier
                .size(16.dp)
                .padding(top = 1.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text     = text,
            fontSize = 13.sp,
            color    = Color.White.copy(alpha = 0.85f)
        )
    }
}

/**
 * Card individual do carrossel de materiais.
 * Exibe a imagem do material e seu nome centralizado abaixo.
 */
@Composable
private fun MaterialChipCard(material: String) {
    val drawable = MaterialDrawableMapper.fromName(material)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
        ) {
            Image(
                painter            = painterResource(drawable),
                contentDescription = material,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text       = material,
            fontSize   = 10.sp,
            color      = Color.White.copy(alpha = 0.9f),
            textAlign  = TextAlign.Center,
            maxLines   = 2,
            overflow   = TextOverflow.Ellipsis,
            lineHeight = 13.sp
        )
    }
}