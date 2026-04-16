package br.recycleapp.ui.components

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import br.recycleapp.domain.map.RecyclingPoint

/**
 * Bottom sheet exibido quando o usuário toca num marcador do mapa.
 *
 * Exibe nome, subtítulo, endereço, referência, horários, benefícios
 * e os ícones individuais dos materiais aceitos pelo ponto.
 *
 * O carrossel de materiais usa [BoxWithConstraints] para calcular o tamanho
 * dos itens dinamicamente, garantindo que sempre apareça ~3.5 itens visíveis
 * independente do tamanho da tela — criando um peek natural do próximo item.
 * Os dots de paginação representam as posições de scroll disponíveis,
 * não o número total de itens.
 *
 * @param point      ponto de coleta selecionado
 * @param sheetColor cor de fundo — deve corresponder ao material da tela
 * @param onDismiss  callback para fechar o sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclingPointBottomSheet(
    point: RecyclingPoint,
    sheetColor: Color = Color(0xFF1565C0),
    onDismiss: () -> Unit
) {
    val context    = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {

            // ── Nome ──────────────────────────────────────────────────
            Text(
                text       = point.name,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )

            // ── Subtítulo ─────────────────────────────────────────────
            if (point.subtitle.isNotEmpty()) {
                Text(
                    text     = point.subtitle,
                    fontSize = 13.sp,
                    color    = Color.White.copy(alpha = 0.75f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Endereço + referência ─────────────────────────────────
            if (point.address.isNotEmpty()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector        = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint               = Color.White.copy(alpha = 0.85f),
                        modifier           = Modifier
                            .padding(top = 4.dp)
                            .size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Column {
                        Text(
                            text     = point.address,
                            fontSize = 13.sp,
                            color    = Color.White.copy(alpha = 0.85f)
                        )
                        if (point.reference.isNotEmpty()) {
                            Text(
                                text     = point.reference,
                                fontSize = 12.sp,
                                color    = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            // ── Horário ───────────────────────────────────────────────
            val hasSchedule = point.scheduleWeekdays.isNotEmpty() ||
                    point.scheduleSaturday.isNotEmpty() ||
                    point.scheduleSunday.isNotEmpty()
            if (hasSchedule) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector        = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint               = Color.White.copy(alpha = 0.85f),
                        modifier           = Modifier
                            .padding(top = 4.dp)
                            .size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Column {
                        if (point.scheduleWeekdays.isNotEmpty()) {
                            Text(
                                text     = point.scheduleWeekdays,
                                fontSize = 13.sp,
                                color    = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        if (point.scheduleSaturday.isNotEmpty()) {
                            Text(
                                text     = point.scheduleSaturday,
                                fontSize = 13.sp,
                                color    = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        if (point.scheduleSunday.isNotEmpty()) {
                            Text(
                                text     = point.scheduleSunday,
                                fontSize = 13.sp,
                                color    = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // ── Benefícios ────────────────────────────────────────────
            val hasBenefits = point.benefitsProgram.isNotEmpty() ||
                    point.benefits.isNotEmpty()
            if (hasBenefits) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector        = Icons.Filled.Redeem,
                        contentDescription = null,
                        tint               = Color.White.copy(alpha = 0.85f),
                        modifier           = Modifier
                            .padding(top = 4.dp)
                            .size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Column {
                        if (point.benefitsProgram.isNotEmpty()) {
                            Text(
                                text       = "Benefício: ${point.benefitsProgram}",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        point.benefits.forEach { benefit ->
                            Row {
                                Text(
                                    text     = "• ",
                                    fontSize = 13.sp,
                                    color    = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text     = benefit,
                                    fontSize = 13.sp,
                                    color    = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
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

            val materialIcons = point.materials
                .map { it.toMaterialDrawable() }
                .ifEmpty { listOf(R.drawable.ic_material_unknown) }

            val listState    = rememberLazyListState()
            val spacing      = 8.dp

            // Quantos itens são visíveis por vez (usado para calcular dots)
            // 3.5 garante que o próximo item sempre apareça cortado na borda,
            // deixando claro visualmente que o carrossel é scrollável
            val visibleCount = 3.5f
            val pageSize     = visibleCount.toInt()  // = 3

            // BoxWithConstraints mede a largura disponível em tempo de composição,
            // permitindo calcular o tamanho exato de cada item para que sempre
            // apareçam exatamente 3.5 itens independente do tamanho da tela
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val itemSize = (maxWidth - spacing * (visibleCount - 1)) / visibleCount

                LazyRow(
                    state                 = listState,
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    items(materialIcons) { res ->
                        Image(
                            painter            = painterResource(res),
                            contentDescription = null,
                            contentScale       = ContentScale.Fit,
                            modifier           = Modifier.size(itemSize)
                        )
                    }
                }
            }

            // ── Dots de paginação ─────────────────────────────────────
            // Exibidos apenas quando há itens fora da área visível.
            // O número de dots representa as posições de scroll disponíveis
            // (total de itens − itens visíveis por vez), não o total de itens.
            // Exemplo: 6 materiais com 3 visíveis → 3 dots (posições 0, 1, 2).
            val numberOfDots = (materialIcons.size - pageSize).coerceAtLeast(0)
            val currentDot by remember(numberOfDots) {
                derivedStateOf {
                    listState.firstVisibleItemIndex.coerceIn(0, (numberOfDots - 1).coerceAtLeast(0))
                }
            }

            if (numberOfDots > 1) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier              = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    (0 until numberOfDots).forEach { index ->
                        val isSelected = currentDot == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 6.dp else 4.dp)
                                .background(
                                    color = if (isSelected) Color.White
                                    else Color.White.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Botão Google Maps ─────────────────────────────────────
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

// ── Mapeamento material → drawable ────────────────────────────────────────────

@DrawableRes
private fun String.toMaterialDrawable(): Int = when (this) {
    "Vidro"             -> R.drawable.ic_material_vidro
    "Plástico"          -> R.drawable.ic_material_plastico
    "Papel"             -> R.drawable.ic_material_papel
    "Metal"             -> R.drawable.ic_material_metal
    "Óleo vegetal"      -> R.drawable.ic_material_oleo_vegetal
    "Pilhas e baterias" -> R.drawable.ic_material_pilhas_baterias
    "Eletrônicos"       -> R.drawable.ic_material_eletronicos
    "Lixo domiciliar"   -> R.drawable.ic_material_lixo_domiciliar
    "Orgânico"          -> R.drawable.ic_material_organico
    "Bens inservíveis"  -> R.drawable.ic_material_bens_inserviveis
    "Entulho"           -> R.drawable.ic_material_entulho
    "Pneus"             -> R.drawable.ic_material_pneus
    "Galhadas"          -> R.drawable.ic_material_galhadas
    else                -> R.drawable.ic_material_unknown
}