package br.recycleapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Botão flutuante de filtro exibido sobre o mapa.
 *
 * Ao ser clicado, abre o [MapFilterBottomSheet] com as opções de filtragem.
 *
 * @param toneColor    cor temática do material atual
 * @param onOpenFilter callback para abrir o bottom sheet de filtros
 */
@Composable
fun MapFilterBar(
    toneColor: Color,
    onOpenFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed         by interactionSource.collectIsPressedAsState()
    val scale             by animateFloatAsState(
        targetValue   = if (isPressed) 0.94f else 1f,
        animationSpec = tween(if (isPressed) 80 else 160),
        label         = "filter_btn_scale"
    )

    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(50.dp))
            .background(toneColor)
            .clickable(
                interactionSource = interactionSource,
                indication        = null       
            ) { onOpenFilter() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector        = Icons.Filled.Tune,
            contentDescription = null,
            tint               = Color.White,
            modifier           = Modifier.size(16.dp)
        )
        Text(
            text       = "Filtro",
            color      = Color.White,
            fontSize   = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}