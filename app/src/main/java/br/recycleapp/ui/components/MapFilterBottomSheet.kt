package br.recycleapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R

/**
 * Bottom sheet de filtros do mapa.
 *
 * Exibe toggles para habilitar/desabilitar a visualização de PEVs e Ecopontos,
 * com um atalho "Habilitar todos" no topo. Segue o padrão visual do
 * [RecyclingPointBottomSheet], usando [toneColor] como fundo.
 *
 * @param showPev          estado atual do filtro de PEVs
 * @param showEcoponto     estado atual do filtro de Ecopontos
 * @param toneColor        cor temática do material atual
 * @param onTogglePev      callback ao alternar filtro de PEVs
 * @param onToggleEcoponto callback ao alternar filtro de Ecopontos
 * @param onDismiss        callback para fechar o sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapFilterBottomSheet(
    showPev: Boolean,
    showEcoponto: Boolean,
    showEcopontoLight: Boolean,
    toneColor: Color,
    onTogglePev: () -> Unit,
    onToggleEcoponto: () -> Unit,
    onToggleEcopontoLight: () -> Unit,
    onDismiss: () -> Unit
) {
    val allEnabled = showPev && showEcoponto && showEcopontoLight

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                    if (allEnabled) {
                        onTogglePev()
                        onToggleEcoponto()
                        onToggleEcopontoLight()
                    } else {
                        if (!showPev)            onTogglePev()
                        if (!showEcoponto)       onToggleEcoponto()
                        if (!showEcopontoLight)  onToggleEcopontoLight()
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

            // ── PEVs ──────────────────────────────────────────────────
            FilterToggleRow(
                label     = "PEVs Comlurb",
                checked   = showPev,
                toneColor = toneColor,
                leadingContent = {
                    Image(
                        painter            = painterResource(R.drawable.pin_pev_rio),
                        contentDescription = null,
                        modifier           = Modifier.size(width = 20.dp, height = 30.dp)
                    )
                },
                onCheckedChange = { onTogglePev() }
            )

            Spacer(Modifier.height(4.dp))

            // ── Ecopontos Comlurb ─────────────────────────────────────
            FilterToggleRow(
                label     = "Ecopontos Comlurb",
                checked   = showEcoponto,
                toneColor = toneColor,
                leadingContent = {
                    Image(
                        painter            = painterResource(R.drawable.pin_ecoponto_rio),
                        contentDescription = null,
                        modifier           = Modifier.size(width = 20.dp, height = 30.dp)
                    )
                },
                onCheckedChange = { onToggleEcoponto() }
            )

            Spacer(Modifier.height(4.dp))

            // ── Ecopontos Light ───────────────────────────────────────
            FilterToggleRow(
                label     = "Ecopontos Light",
                checked   = showEcopontoLight,
                toneColor = toneColor,
                leadingContent = {
                    Image(
                        painter            = painterResource(R.drawable.pin_ecoponto_light),
                        contentDescription = null,
                        modifier           = Modifier.size(width = 20.dp, height = 30.dp)
                    )
                },
                onCheckedChange = { onToggleEcopontoLight() }
            )
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