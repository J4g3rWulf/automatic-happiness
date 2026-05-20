package br.recycleapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R
import br.recycleapp.ui.theme.GreenDark
import br.recycleapp.ui.theme.GreenPrimary

private const val GRID_COLUMNS = 4

// ── Modelo de dados ────

private data class TermItem(
    val buttonImageRes: Int,
    val contentDescription: String,
    val popupTitle: String,
    val popupCards: List<Int>
)

// ── Dados estáticos ────

private val RECYCLING_CONCEPTS = listOf(
    TermItem(
        buttonImageRes     = R.drawable.btn_terms_fundamentos,
        contentDescription = "Fundamentos",
        popupTitle         = "Fundamentos",
        popupCards         = listOf(
            R.drawable.card_verde_1,
            R.drawable.card_verde_2,
            R.drawable.card_verde_3
        )
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_terms_acoes_praticas,
        contentDescription = "Ações Práticas",
        popupTitle         = "Ações Práticas",
        popupCards         = listOf(
            R.drawable.card_azul_1,
            R.drawable.card_azul_2,
            R.drawable.card_azul_3
        )
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_terms_infraestrutura,
        contentDescription = "Infraestrutura",
        popupTitle         = "Infraestrutura",
        popupCards         = listOf(
            R.drawable.card_laranja_1,
            R.drawable.card_laranja_2,
            R.drawable.card_laranja_3
        )
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_terms_sistema,
        contentDescription = "Sistema",
        popupTitle         = "Sistema",
        popupCards         = listOf(
            R.drawable.card_roxo_1,
            R.drawable.card_roxo_2,
            R.drawable.card_roxo_3
        )
    )
)

private val PLASTIC_NUMBERS = listOf(
    TermItem(
        buttonImageRes     = R.drawable.btn_plastic_pet,
        contentDescription = "PET",
        popupTitle         = "Números do Plástico",
        popupCards         = listOf(R.drawable.card_info_plastic_1_pet)
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_plastic_pead,
        contentDescription = "PEAD",
        popupTitle         = "Números do Plástico",
        popupCards         = listOf(R.drawable.card_info_plastic_2_pead)
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_plastic_pvc,
        contentDescription = "PVC",
        popupTitle         = "Números do Plástico",
        popupCards         = listOf(R.drawable.card_info_plastic_3_pvc)
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_plastic_pebd,
        contentDescription = "PEBD",
        popupTitle         = "Números do Plástico",
        popupCards         = listOf(R.drawable.card_info_plastic_4_pebd)
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_plastic_pp,
        contentDescription = "PP",
        popupTitle         = "PP",
        popupCards         = listOf(R.drawable.card_info_plastic_5_pp)
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_plastic_ps,
        contentDescription = "PS",
        popupTitle         = "Números do Plástico",
        popupCards         = listOf(R.drawable.card_info_plastic_6_ps)
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_plastic_outros,
        contentDescription = "Outros",
        popupTitle         = "Números do Plástico",
        popupCards         = listOf(R.drawable.card_info_plastic_7_outros)
    )
)

// ── Tela Principal ────

@Composable
fun TermsScreen(
    onBack: () -> Unit
) {
    var selectedTerm by remember { mutableStateOf<TermItem?>(null) }
    val popupVisible = selectedTerm != null

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Conteúdo principal (com blur quando popup visível) ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (popupVisible) Modifier.blur(8.dp) else Modifier)
        ) {
            TermsScreenContent(
                onBack         = onBack,
                popupVisible   = popupVisible,
                onTermSelected = { term -> selectedTerm = term }
            )
        }

        // ── Overlay escuro + popup ──
        AnimatedVisibility(
            visible = popupVisible,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { selectedTerm = null },
                contentAlignment = Alignment.Center
            ) {
                selectedTerm?.let { term ->
                    TermPopup(
                        term    = term,
                        onClose = { selectedTerm = null }
                    )
                }
            }
        }
    }
}

// ── Conteúdo scrollável da tela ────

@Composable
private fun TermsScreenContent(
    onBack         : () -> Unit,
    popupVisible   : Boolean,
    onTermSelected : (TermItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary)
    ) {
        // Arte decorativa superior
        Image(
            painter            = painterResource(R.drawable.art_top_v1),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.TopCenter,
            modifier           = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // Arte decorativa inferior
        Image(
            painter            = painterResource(R.drawable.art_botton_learn_screen),
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
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(22.dp))

            // ── Botão Voltar ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .background(color = GreenDark, shape = CircleShape)
                        .border(width = 2.dp, color = Color.White, shape = CircleShape)
                        .clickable(enabled = !popupVisible) { onBack() },
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

            Spacer(Modifier.height(20.dp))

            // ── Título ──
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(11.dp),
                color           = Color.White,
                shadowElevation = 4.dp
            ) {
                Text(
                    text       = "Termos e definições",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GreenDark,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Card de seção verde — Conceitos de Reciclagem ──
            TermsSectionCard(
                backgroundImageRes = R.drawable.bg_section_terms_green,
                title              = "Conceitos de Reciclagem",
                items              = RECYCLING_CONCEPTS,
                popupVisible       = popupVisible,
                onTermSelected     = onTermSelected
            )

            Spacer(Modifier.height(16.dp))

            // ── Card de seção vermelho — Guia de plásticos ──
            TermsSectionCard(
                backgroundImageRes = R.drawable.bg_section_terms_red,
                title              = "Números de reciclagem do plástico",
                items              = PLASTIC_NUMBERS,
                useCustomLayout    = true,
                popupVisible       = popupVisible,
                onTermSelected     = onTermSelected
            )

            Spacer(Modifier.height(200.dp))
        }
    }
}

// ── Card de seção ────

@Composable
private fun TermsSectionCard(
    backgroundImageRes : Int,
    title              : String,
    items              : List<TermItem>,
    useCustomLayout    : Boolean = false,
    popupVisible       : Boolean,
    onTermSelected     : (TermItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Image(
            painter            = painterResource(backgroundImageRes),
            contentDescription = null,
            contentScale       = ContentScale.FillBounds,
            modifier           = Modifier
                .matchParentSize()
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Título do card
            Text(
                text       = title,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            if (useCustomLayout) {
                // ── Layout 4+3 para o card vermelho ──
                val firstRow  = items.take(4)
                val secondRow = items.drop(4)

                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Linha 1 - 4 botões
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        firstRow.forEach { term ->
                            Image(
                                painter            = painterResource(term.buttonImageRes),
                                contentDescription = term.contentDescription,
                                modifier           = Modifier
                                    .size(72.dp)
                                    .clickable(
                                        enabled           = !popupVisible,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication        = null
                                    ) { onTermSelected(term) }
                            )
                        }
                    }

                    // Linha 2 - 3 botões centralizados
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        secondRow.forEach { term ->
                            Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                                Image(
                                    painter            = painterResource(term.buttonImageRes),
                                    contentDescription = term.contentDescription,
                                    modifier           = Modifier
                                        .size(72.dp)
                                        .clickable(
                                            enabled           = !popupVisible,
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication        = null
                                        ) { onTermSelected(term) }
                                )
                            }
                        }
                    }
                }
            } else {
                // ── Grid fixo de botões (4 por linha) ──
                val rows = items.chunked(GRID_COLUMNS)
                rows.forEach { rowItems ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowItems.forEach { term ->
                            Image(
                                painter            = painterResource(term.buttonImageRes),
                                contentDescription = term.contentDescription,
                                modifier           = Modifier
                                    .size(65.dp)
                                    .clickable(
                                        enabled           = !popupVisible,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication        = null
                                    ) { onTermSelected(term) }
                            )
                        }
                        repeat(GRID_COLUMNS - rowItems.size) {
                            Spacer(Modifier.size(70.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

// ── PopUp informativo ────

@Composable
private fun TermPopup(
    term   : TermItem,
    onClose: () -> Unit
) {
    val isSingleCard = term.popupCards.size == 1

    Box(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .then(if (isSingleCard) Modifier.fillMaxHeight().statusBarsPadding() else Modifier.fillMaxHeight(0.85f))
            // Consome cliques dentro do popup para não fechar ao clicar nele
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { /* consome o clique */ },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSingleCard) {
                Spacer(Modifier.height(82.dp))
            }

            // ── Título do popup ──
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp),
                color           = Color.White,
                shadowElevation = 6.dp
            ) {
                Text(
                    text       = term.popupTitle,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GreenDark,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.padding(vertical = 10.dp, horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(12.dp))


            // ── Área dos cards (Centralizado se for único, Scroll se forem vários) ──
            if (term.popupCards.size == 1) {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter            = painterResource(term.popupCards.first()),
                        contentDescription = term.popupTitle,
                        contentScale       = ContentScale.FillWidth,
                        modifier           = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    term.popupCards.forEach { cardRes ->
                        Image(
                            painter            = painterResource(cardRes),
                            contentDescription = term.popupTitle,
                            contentScale       = ContentScale.FillWidth,
                            modifier           = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Botão Fechar circular com seta ──
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .background(color = Color.White, shape = CircleShape)
                        .border(width = 2.dp, color = GreenDark, shape = CircleShape)
                        .clickable(
                            enabled           = true,
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Fechar popup",
                        tint               = GreenDark,
                        modifier           = Modifier.size(24.dp)
                    )
                }
            }

            if (isSingleCard) {
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}