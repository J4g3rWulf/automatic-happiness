package br.recycleapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.domain.map.PointType
import br.recycleapp.domain.map.toFilterLabel
import br.recycleapp.domain.map.toPinDrawable

/**
 * Bottom sheet de filtros do mapa.
 *
 * Exibe um toggle individual para cada [PointType] presente em [typeVisibility].
 * A ordem dos tipos segue a declaração do enum, garantindo consistência visual.
 *
 * Para adicionar um novo tipo ao filtro, basta adicioná-lo ao enum [PointType]
 * — ele aparecerá automaticamente aqui, visível por padrão.
 *
 * @param typeVisibility mapa de visibilidade por tipo — chave: tipo, valor: visível
 * @param onToggle       callback ao alternar um tipo específico
 * @param toneColor      cor temática do material atual
 * @param onDismiss      callback para fechar o sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFilterBottomSheet(
    typeVisibility: Map<PointType, Boolean>,
    onToggle: (PointType) -> Unit,
    toneColor: Color,
    onDismiss: () -> Unit
) {
    // Tipos exibidos no filtro — exclui UNKNOWN, usado internamente como
    // fallback para resultados da Places API sem tipo definido
    val displayedTypes = PointType.entries.filter { type ->
        type != PointType.UNKNOWN
    }

    val allEnabled = displayedTypes.all { typeVisibility[it] != false }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor   = toneColor,
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

            Text(
                text       = "Filtros",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )

            Spacer(Modifier.height(16.dp))

            // ── Habilitar todos ───────────────────────────────────────
            FilterToggleRow(
                label     = "Habilitar todos",
                checked   = allEnabled,
                bold      = true,
                toneColor = toneColor,
                onCheckedChange = {
                    displayedTypes.forEach { type ->
                        val isVisible = typeVisibility[type] != false
                        // Se todos estão ligados, desliga todos. Senão, liga os que estão desligados.
                        if (allEnabled && isVisible) onToggle(type)
                        else if (!allEnabled && !isVisible) onToggle(type)
                    }
                }
            )

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))
            Spacer(Modifier.height(4.dp))

            Text(
                text     = "TIPOS DE PONTO",
                fontSize = 11.sp,
                color    = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ── Toggle por tipo ───────────────────────────────────────
            // Gerado automaticamente a partir do enum — novos tipos aparecem aqui
            displayedTypes.forEach { type ->
                FilterToggleRow(
                    label     = type.toFilterLabel(),
                    checked   = typeVisibility[type] != false,
                    toneColor = toneColor,
                    leadingContent = {
                        Image(
                            painter            = painterResource(type.toPinDrawable()),
                            contentDescription = null,
                            modifier           = Modifier.size(width = 20.dp, height = 30.dp)
                        )
                    },
                    onCheckedChange = { onToggle(type) }
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun FilterToggleRow(
    label: String,
    checked: Boolean,
    toneColor: Color,
    bold: Boolean = false,
    leadingContent: @Composable (() -> Unit)? = null,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            leadingContent?.invoke()
            Text(
                text       = label,
                fontSize   = 15.sp,
                fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
                color      = Color.White
            )
        }

        Switch(
            checked         = checked,
            onCheckedChange = { onCheckedChange() },
            colors          = SwitchDefaults.colors(
                checkedThumbColor    = toneColor,
                checkedTrackColor    = Color.White,
                uncheckedThumbColor  = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor  = Color.White.copy(alpha = 0.2f),
                uncheckedBorderColor = Color.White.copy(alpha = 0.35f)
            )
        )
    }
}