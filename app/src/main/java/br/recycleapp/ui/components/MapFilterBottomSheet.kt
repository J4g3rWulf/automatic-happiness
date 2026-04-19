package br.recycleapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import br.recycleapp.ui.mapper.MaterialDrawableMapper

/**
 * Bottom sheet de filtros do mapa.
 *
 * Exibe duas seções colapsáveis independentemente, ambas iniciando fechadas:
 *  - **Tipos de ponto** — toggle por [PointType]
 *  - **Materiais aceitos** — toggle por nome de material (derivado dinamicamente
 *    dos pontos carregados; só aparece se ao menos um ponto declara o material)
 *
 * Cada seção possui seu próprio toggle "Habilitar todos" independente.
 *
 * @param typeVisibility     mapa de visibilidade por tipo de ponto
 * @param onToggle           callback ao alternar um tipo específico
 * @param materialVisibility mapa de visibilidade por material (chave = nome do material)
 * @param onMaterialToggle   callback ao alternar um material específico
 * @param toneColor          cor temática do material atual
 * @param onDismiss          callback para fechar o sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFilterBottomSheet(
    typeVisibility: Map<PointType, Boolean>,
    onToggle: (PointType) -> Unit,
    materialVisibility: Map<String, Boolean>,
    onMaterialToggle: (String) -> Unit,
    toneColor: Color,
    onDismiss: () -> Unit
) {
    // Tipos exibidos no filtro — exclui UNKNOWN, usado internamente como fallback
    val displayedTypes = PointType.entries.filter { it != PointType.UNKNOWN }

    // Materiais ordenados conforme lista de prioridade definida no repositório
    val displayedMaterials = materialVisibility.keys.toList()

    // Estado de "habilitar todos" calculado por seção independentemente
    val allTypesEnabled     = displayedTypes.all { typeVisibility[it] != false }
    val allMaterialsEnabled = displayedMaterials.all { materialVisibility[it] != false }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Estado de colapso de cada seção — ambas iniciam fechadas
    var typeSectionExpanded     by remember { mutableStateOf(false) }
    var materialSectionExpanded by remember { mutableStateOf(false) }

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

            // ── Título ────────────────────────────────────────────────────
            Text(
                text       = "Filtros",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.25f))

            // ── Seção: Tipos de ponto ─────────────────────────────────────
            CollapsibleSectionHeader(
                label    = "TIPOS DE PONTO",
                expanded = typeSectionExpanded,
                onToggle = { typeSectionExpanded = !typeSectionExpanded }
            )

            AnimatedVisibility(
                visible = typeSectionExpanded,
                enter   = expandVertically(),
                exit    = shrinkVertically()
            ) {
                Column {
                    FilterToggleRow(
                        label     = "Habilitar todos",
                        checked   = allTypesEnabled,
                        bold      = true,
                        toneColor = toneColor,
                        onCheckedChange = {
                            displayedTypes.forEach { type ->
                                val isVisible = typeVisibility[type] != false
                                if (allTypesEnabled && isVisible) onToggle(type)
                                else if (!allTypesEnabled && !isVisible) onToggle(type)
                            }
                        }
                    )
                    Spacer(Modifier.height(4.dp))
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

            // ── Seção: Materiais aceitos ──────────────────────────────────
            // Só renderiza se ao menos um ponto declara materiais
            if (displayedMaterials.isNotEmpty()) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.25f))

                CollapsibleSectionHeader(
                    label    = "MATERIAIS ACEITOS",
                    expanded = materialSectionExpanded,
                    onToggle = { materialSectionExpanded = !materialSectionExpanded }
                )

                AnimatedVisibility(
                    visible = materialSectionExpanded,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    Column {
                        FilterToggleRow(
                            label     = "Habilitar todos",
                            checked   = allMaterialsEnabled,
                            bold      = true,
                            toneColor = toneColor,
                            onCheckedChange = {
                                displayedMaterials.forEach { mat ->
                                    val isVisible = materialVisibility[mat] != false
                                    if (allMaterialsEnabled && isVisible) onMaterialToggle(mat)
                                    else if (!allMaterialsEnabled && !isVisible) onMaterialToggle(mat)
                                }
                            }
                        )
                        Spacer(Modifier.height(4.dp))
                        displayedMaterials.forEach { material ->
                            FilterToggleRow(
                                label     = material,
                                checked   = materialVisibility[material] != false,
                                toneColor = toneColor,
                                leadingContent = {
                                    Image(
                                        painter            = painterResource(
                                            MaterialDrawableMapper.fromName(material)
                                        ),
                                        contentDescription = null,
                                        modifier           = Modifier.size(28.dp)
                                    )
                                },
                                onCheckedChange = { onMaterialToggle(material) }
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Cabeçalho de seção colapsável ────────────────────────────────────────────

@Composable
private fun CollapsibleSectionHeader(
    label: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onToggle
            )
            .padding(vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text       = label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color      = Color.White.copy(alpha = 0.9f)
        )
        Icon(
            imageVector        = if (expanded) Icons.Filled.KeyboardArrowUp
            else          Icons.Filled.KeyboardArrowDown,
            contentDescription = if (expanded) "Recolher" else "Expandir",
            tint               = Color.White.copy(alpha = 0.9f),
            modifier           = Modifier.size(18.dp)
        )
    }
}

// ── Linha de toggle genérica ──────────────────────────────────────────────────

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