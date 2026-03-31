package br.recycleapp.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.ui.theme.WhiteText

/**
 * Botão preenchido padrão - usado nas ações principais da ResultScreen.
 *
 * @param text           texto exibido no botão
 * @param onClick        ação ao clicar
 * @param containerColor cor de fundo do botão
 * @param modifier       modifier externo para peso/tamanho no layout pai
 */
@Composable
fun ResultButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick        = onClick,
        modifier       = modifier.height(56.dp),
        shape          = RoundedCornerShape(102.dp),
        colors         = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor   = WhiteText
        ),
        elevation      = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
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