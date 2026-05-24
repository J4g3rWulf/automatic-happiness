package br.recycleapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.ui.theme.WhiteText

@Composable
fun ResultButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed         by interactionSource.collectIsPressedAsState()
    val scale             by animateFloatAsState(
        targetValue   = if (isPressed) 0.96f else 1f,
        animationSpec = tween(if (isPressed) 80 else 160),
        label         = "result_btn_scale"
    )

    Button(
        onClick           = onClick,
        interactionSource = interactionSource,
        modifier          = modifier
            .height(56.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape             = RoundedCornerShape(102.dp),
        colors            = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor   = WhiteText
        ),
        elevation         = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        ),
        contentPadding    = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(
            text      = text,
            fontSize  = 13.sp,
            maxLines  = 1,
            color     = WhiteText,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Variante outlined - para ações secundárias ou desabilitadas visualmente.
 *
 * @param text        texto exibido no botão
 * @param onClick     ação ao clicar
 * @param borderColor cor da borda e do texto
 * @param modifier    modifier externo para peso/tamanho no layout pai
 */
@Suppress("unused")    // SERÁ USADA FUTURAMENTE
@Composable
fun ResultOutlinedButton(
    text: String,
    onClick: () -> Unit,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick        = onClick,
        modifier       = modifier.height(56.dp),
        shape          = RoundedCornerShape(102.dp),
        colors         = ButtonDefaults.outlinedButtonColors(
            contentColor = borderColor
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(
            text      = text,
            fontSize  = 13.sp,
            maxLines  = 1,
            textAlign = TextAlign.Center
        )
    }
}