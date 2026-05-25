package br.recycleapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R
import br.recycleapp.domain.map.RecyclingPoint
import br.recycleapp.ui.theme.TextSecondary

/**
 * Dados visuais de um material — cores, recursos e strings.
 * Movido de ResultScreen para permitir reuso em outros contextos.
 */
data class MaterialCardData(
    val tone: Color,
    val cardTitleColor: Color,
    val cardTitle: Int,
    val tip1: Int,
    val tip2: Int,
    val mapColor: Color = tone
)

/**
 * Card branco com título, duas dicas de descarte e mapa de pontos próximos.
 * Usado na ResultScreen para materiais identificados.
 *
 * O [RecycleMapCard] é chamado com sizing explícito (height + clip) —
 * seguindo o padrão onde o caller define as dimensões do mapa.
 */
@Composable
fun MaterialCard(
    data: MaterialCardData,
    modifier: Modifier = Modifier,
    onMarkerClick: (RecyclingPoint) -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = Color.White
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 9.dp)) {

            Text(
                text  = stringResource(data.cardTitle),
                color = data.cardTitleColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            )

            Spacer(Modifier.height(2.dp))

            BulletText(text = stringResource(data.tip1), color = data.tone, fontSize = 13.sp, startPadding = 8.dp)
            Spacer(Modifier.height(2.dp))
            BulletText(text = stringResource(data.tip2), color = data.tone, fontSize = 13.sp, startPadding = 8.dp)

            Spacer(Modifier.height(14.dp))

            Text(
                text  = stringResource(R.string.result_map_title),
                color = data.cardTitleColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp
                )
            )

            Spacer(Modifier.height(10.dp))

            // Sizing e clip são definidos aqui pelo caller — RecycleMapCard
            // não impõe dimensões próprias (ver KDoc de RecycleMapCard).
            RecycleMapCard(
                toneColor     = data.mapColor,
                modifier      = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp)),
                onMarkerClick = onMarkerClick
            )
        }
    }
}

/**
 * Dois cards empilhados para o caso Indefinido/Desconhecido.
 */
@Composable
fun UnknownCard(
    toneColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            color    = Color.White
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 9.dp)) {
                Text(
                    text  = stringResource(R.string.result_unknown_title),
                    color = toneColor,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = stringResource(R.string.result_unknown_subtitle),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            color    = Color.White
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 9.dp)) {
                Text(
                    text  = stringResource(R.string.result_unknown_card2_title),
                    color = toneColor,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = stringResource(R.string.result_unknown_card2_subtitle),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                )
                Spacer(Modifier.height(12.dp))
                Image(
                    painter            = painterResource(R.drawable.result_card_unknown),
                    contentDescription = null,
                    contentScale       = ContentScale.FillWidth,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

/**
 * Linha de texto com bullet point à esquerda.
 * Componente interno — usado por [MaterialCard] e [UnknownCard].
 */
@Composable
internal fun BulletText(
    text: String,
    color: Color,
    fontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize,
    startPadding: Dp = 0.dp
) {
    Row(
        modifier          = Modifier.padding(start = startPadding),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "• ", color = color, fontSize = fontSize)
        Text(text = text,  color = color, fontSize = fontSize)
    }
}