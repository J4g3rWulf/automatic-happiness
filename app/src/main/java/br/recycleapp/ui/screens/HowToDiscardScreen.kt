package br.recycleapp.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R
import br.recycleapp.ui.theme.GreenDark
import br.recycleapp.ui.theme.GreenPrimary

// ── Modelo de dados e Lista estática ────

private data class HowToDiscardCard(
    val name: String,
    val cardColor: Color,
    val imageRes: Int
)

private val HOW_TO_DISCARD_CARDS = listOf(
    HowToDiscardCard("Madeira",    Color.Transparent, R.drawable.card_separar_madeira),
    HowToDiscardCard("Vidro",      Color.Transparent, R.drawable.card_separar_vidro),
    HowToDiscardCard("Plástico",   Color.Transparent, R.drawable.card_separar_plastico),
    HowToDiscardCard("Papel",      Color.Transparent, R.drawable.card_separar_papel),
    HowToDiscardCard("Metal",      Color.Transparent, R.drawable.card_separar_metal),
    HowToDiscardCard("Perigosos",  Color.Transparent, R.drawable.card_separar_perigosos),
    HowToDiscardCard("Orgânico",   Color.Transparent, R.drawable.card_separar_organico),
    HowToDiscardCard("Rejeito",    Color.Transparent, R.drawable.card_separar_rejeito)
)

// ── Tela Principal ────

@Composable
fun HowToDiscardScreen(
    onBack: () -> Unit
) {
    val startIndex = (HOW_TO_DISCARD_CARDS.size * 1000) + 1
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { Int.MAX_VALUE }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary)
    ) {

        Image(
            painter            = painterResource(R.drawable.learn_art_top),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.TopCenter,
            modifier           = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        Image(
            painter            = painterResource(R.drawable.learn_art_bottom),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.BottomCenter,
            modifier           = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {

            Spacer(Modifier.height(22.dp))

            // ── 1. Botão de Voltar ──
            val backInteraction = remember { MutableInteractionSource() }
            val backPressed     by backInteraction.collectIsPressedAsState()
            val backScale       by animateFloatAsState(
                targetValue   = if (backPressed) 0.88f else 1f,
                animationSpec = tween(if (backPressed) 80 else 160),
                label         = "back_btn_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer { scaleX = backScale; scaleY = backScale }
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .background(color = GreenDark, shape = CircleShape)
                        .border(width = 2.dp, color = Color.White, shape = CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    backInteraction.emit(press)
                                    val released = tryAwaitRelease()
                                    if (released) {
                                        backInteraction.emit(PressInteraction.Release(press))
                                        onBack()
                                    } else {
                                        backInteraction.emit(PressInteraction.Cancel(press))
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint               = Color.White,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── 2. Título ──
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier        = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    shape           = RoundedCornerShape(11.dp),
                    color           = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text       = "Como descartar ?",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = GreenDark,
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── 3. Carrossel ──
            HorizontalPager(
                state          = pagerState,
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing    = 16.dp,
                modifier       = Modifier.weight(1f)
            ) { page ->
                val actualIndex = page % HOW_TO_DISCARD_CARDS.size
                HowToDiscardCardItem(card = HOW_TO_DISCARD_CARDS[actualIndex])
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// ── Card do carrossel ────

@Composable
private fun HowToDiscardCardItem(card: HowToDiscardCard) {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f),
            shape     = RoundedCornerShape(10.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            )
        ) {
            Image(
                painter            = painterResource(id = card.imageRes),
                contentDescription = card.name,
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Fit
            )
        }
    }
}