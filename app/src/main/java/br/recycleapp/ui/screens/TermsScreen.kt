package br.recycleapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

private val PACKAGING_SYMBOLS = listOf(
    TermItem(
        buttonImageRes     = R.drawable.btn_terms_reciclagem_universal,
        contentDescription = "Reciclagem Universal",
        popupTitle         = "Símbolos de Embalagens",
        popupCards         = listOf(R.drawable.card_info_embalagem_1)
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_terms_aluminio_reciclavel,
        contentDescription = "Alumínio",
        popupTitle         = "Símbolos de Embalagens",
        popupCards         = listOf(R.drawable.card_info_embalagem_2)
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_terms_aco_reciclavel,
        contentDescription = "Aço",
        popupTitle         = "Símbolos de Embalagens",
        popupCards         = listOf(R.drawable.card_info_embalagem_3)
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_terms_lixo_eletronico,
        contentDescription = "Lixo Eletrônico",
        popupTitle         = "Símbolos de Embalagens",
        popupCards         = listOf(R.drawable.card_info_embalagem_4)
    ),
    TermItem(
        buttonImageRes     = R.drawable.btn_terms_ponto_verde,
        contentDescription = "Ponto Verde",
        popupTitle         = "Símbolos de Embalagens",
        popupCards         = listOf(R.drawable.card_info_embalagem_5)
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
        popupTitle         = "Números do Plástico",
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
            painter            = painterResource(R.drawable.learn_art_top),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.TopCenter,
            modifier           = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // Arte decorativa inferior
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(22.dp))

            // ── Botão Voltar ──
            val backInteraction = remember { MutableInteractionSource() }
            val backPressed     by backInteraction.collectIsPressedAsState()
            val backScale       by animateFloatAsState(
                targetValue   = if (backPressed) 0.88f else 1f,
                animationSpec = tween(if (backPressed) 80 else 160),
                label         = "back_btn_scale"
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer { scaleX = backScale; scaleY = backScale }
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .background(color = GreenDark, shape = CircleShape)
                    .border(width = 2.dp, color = Color.White, shape = CircleShape)
                    .pointerInput(popupVisible) {
                        detectTapGestures(
                            onPress = { offset ->
                                if (popupVisible) return@detectTapGestures
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

            Spacer(Modifier.height(18.dp))

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

            Spacer(Modifier.height(18.dp))

            // ── Card verde — Conceitos de Reciclagem (grid fixo, sem scroll) ──
            TermsSectionCard(
                backgroundImageRes = R.drawable.bg_section_terms_green,
                title              = "Conceitos de Reciclagem",
                items              = RECYCLING_CONCEPTS,
                bottomPadding      = 12.dp,
                popupVisible       = popupVisible,
                onTermSelected     = onTermSelected
            )

            Spacer(Modifier.height(10.dp))

            // ── Card azul — Símbolos de Embalagens (scroll horizontal) ──
            TermsSectionCard(
                backgroundImageRes  = R.drawable.bg_section_terms_blue,
                title               = "Símbolos de Embalagens",
                items               = PACKAGING_SYMBOLS,
                useHorizontalScroll = true,
                bottomPadding       = 22.dp,
                popupVisible        = popupVisible,
                onTermSelected      = onTermSelected
            )

            Spacer(Modifier.height(10.dp))

            // ── Card vermelho — Números do Plástico (scroll horizontal) ──
            TermsSectionCard(
                backgroundImageRes  = R.drawable.bg_section_terms_red,
                title               = "Números de reciclagem do plástico",
                items               = PLASTIC_NUMBERS,
                useHorizontalScroll = true,
                bottomPadding       = 15.dp,
                popupVisible        = popupVisible,
                onTermSelected      = onTermSelected
            )
        }
    }
}

// ── Card de seção ────

@Composable
private fun TermsSectionCard(
    backgroundImageRes : Int,
    title              : String,
    items              : List<TermItem>,
    useHorizontalScroll: Boolean = false,
    bottomPadding      : Dp      = 10.dp,
    popupVisible       : Boolean,
    onTermSelected     : (TermItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Imagem de fundo
        Image(
            painter            = painterResource(backgroundImageRes),
            contentDescription = null,
            contentScale       = ContentScale.FillBounds,
            modifier           = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = bottomPadding)
        ) {
            // Título do card
            Text(
                text       = title,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
                textAlign  = TextAlign.Center,
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(10.dp))

            if (useHorizontalScroll) {
                // ── Scroll horizontal ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                ) {
                    LazyRow(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding        = PaddingValues(horizontal = 10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        items(items) { term ->
                            TermImageButton(
                                term           = term,
                                enabled        = !popupVisible,
                                onTermSelected = onTermSelected
                            )
                        }
                    }
                }
            } else {
                // ── Grid fixo de botões (4 por linha, sem scroll) ──
                val rows = items.chunked(GRID_COLUMNS)
                rows.forEach { rowItems ->
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                    ) {
                        rowItems.forEach { term ->
                            TermImageButton(
                                term           = term,
                                enabled        = !popupVisible,
                                onTermSelected = onTermSelected
                            )
                        }
                        // Spacers para manter o alinhamento na última linha incompleta
                        repeat(GRID_COLUMNS - rowItems.size) {
                            Spacer(Modifier.size(64.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ── Botão de imagem com efeito de pressionamento ────

@Composable
private fun TermImageButton(
    term           : TermItem,
    enabled        : Boolean,
    onTermSelected : (TermItem) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed         by interactionSource.collectIsPressedAsState()
    val scale             by animateFloatAsState(
        targetValue   = if (isPressed) 0.88f else 1f,
        animationSpec = tween(if (isPressed) 80 else 160),
        label         = "term_img_btn_scale"
    )

    Image(
        painter            = painterResource(term.buttonImageRes),
        contentDescription = term.contentDescription,
        modifier           = Modifier
            .size(64.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .pointerInput(term, enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        val released = tryAwaitRelease()
                        if (released) {
                            interactionSource.emit(PressInteraction.Release(press))
                            onTermSelected(term)
                        } else {
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    }
                )
            }
    )
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { /* consome o clique para não fechar ao tocar no popup */ },
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

            // ── Área dos cards ──
            if (isSingleCard) {
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
                modifier         = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val closeInteraction = remember { MutableInteractionSource() }
                val closePressed     by closeInteraction.collectIsPressedAsState()
                val closeScale       by animateFloatAsState(
                    targetValue   = if (closePressed) 0.88f else 1f,
                    animationSpec = tween(if (closePressed) 80 else 160),
                    label         = "close_btn_scale"
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer { scaleX = closeScale; scaleY = closeScale }
                        .shadow(elevation = 6.dp, shape = CircleShape)
                        .background(color = Color.White, shape = CircleShape)
                        .border(width = 2.dp, color = GreenDark, shape = CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    closeInteraction.emit(press)
                                    val released = tryAwaitRelease()
                                    if (released) {
                                        closeInteraction.emit(PressInteraction.Release(press))
                                        onClose()
                                    } else {
                                        closeInteraction.emit(PressInteraction.Cancel(press))
                                    }
                                }
                            )
                        },
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