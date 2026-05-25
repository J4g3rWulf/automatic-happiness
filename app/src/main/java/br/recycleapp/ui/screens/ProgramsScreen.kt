package br.recycleapp.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.indication
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import br.recycleapp.R
import br.recycleapp.ui.theme.GreenDark
import br.recycleapp.ui.theme.GreenPrimary

// ── Modelo de dados ───────────────────────────────────────────────────────────

/**
 * Representa um programa ou parceria de reciclagem.
 *
 * @param name            nome do programa exibido no cabeçalho do popup
 * @param description     texto informativo exibido no corpo do popup
 * @param color           cor de fundo do cabeçalho do popup (identidade visual)
 * @param url             link aberto pelo botão "Saiba mais"
 * @param cardDrawable    drawable exibido como botão na lista de programas
 * @param popupCards      drawables exibidos como cards visuais no popup
 * @param showDescription se verdadeiro, exibe a descrição textual no popup;
 *                        use false quando o card visual já contém todas as
 *                        informações necessárias (ex: Troque e Ganhe)
 * @param titleColor
 */
private data class Program(
    val name            : String,
    val description     : String,
    val color           : Color,
    val url             : String,
    val cardDrawable    : Int,
    val popupCards      : List<Int>,
    val showDescription : Boolean = true,
    val titleColor      : Color?  = null,
)

/**
 * Seção de programas agrupados por categoria.
 *
 * @param title    título da seção (ex: "Troque e Ganhe")
 * @param programs lista de programas da seção
 */
private data class ProgramSection(
    val title    : String,
    val programs : List<Program>,
)

// ── Dados dos programas ───────────────────────────────────────────────────────

// As descriptions estão preservadas para uso futuro
// (ex: tela de detalhe do programa). Atualmente showDescription
// = false em todos os programas pois os cards visuais já
// comunicam o conteúdo de forma completa.

private val PROGRAMS = listOf(

    // ── Troque e Ganhe ────────────────────────────────────────────────────────

    ProgramSection(
        title = "Troque e Ganhe",
        programs = listOf(

            Program(
                name            = "Recicla Niterói\nRecicla PUDs",
                description     = "Leve recicláveis às Plataformas Urbanas Digitais (PUDs) de Niterói " +
                        "e acumule pontos no sistema Desafio Conectado — trocáveis por prêmios.\n\n" +
                        "📋 Como participar\n" +
                        "Cadastre-se com seu CPF na PUD do seu bairro e entregue os recicláveis. " +
                        "Cada entrega gera 100 pontos automaticamente.\n\n" +
                        "♻️ Aceita: recicláveis em geral e eletroeletrônicos\n" +
                        "📅 Seg–Sex 9h–19h | Sáb 9h30–12h30\n\n" +
                        "O material coletado vai para a cooperativa Reciclanit, " +
                        "gerando renda digna para catadores parceiros da CLIN.",
                color           = Color(0xFF1A3A7E),
                titleColor = Color.White,
                url             = "https://plataformadigital.niteroi.br/",
                cardDrawable    = R.drawable.programs_btn_recicla_niteroi,
                popupCards      = listOf(R.drawable.programs_card_troque_1),
                showDescription = false,
            ),

            Program(
                name            = "Light\nRecicla",
                description     = "Leve recicláveis limpos a um ecoponto Light e ganhe crédito " +
                        "direto na sua conta de energia — pelo cartão físico ou pelo app da Light.\n\n" +
                        "📋 Como participar\n" +
                        "Cadastre-se em qualquer ecoponto (leve sua conta de luz). " +
                        "O material é pesado na hora e o bônus entra automaticamente.\n\n" +
                        "♻️ Aceita: Papel · Plástico · Vidro · Metal · Óleo vegetal\n\n" +
                        "Quanto mais você entrega, maior o desconto. " +
                        "Você também pode doar seu bônus para uma das 44 instituições " +
                        "sociais cadastradas no programa.",
                color           = Color(0xFF1C7B6E),
                titleColor = Color.White,
                url             = "https://www.light.com.br",
                cardDrawable    = R.drawable.programs_btn_light_recicla,
                popupCards      = listOf(R.drawable.programs_card_troque_2),
                showDescription = false,
            ),

            Program(
                name            = "Recicla\nSão Gonçalo",
                description     = "Leve seus recicláveis a um dos Ecopontos de São Gonçalo e receba " +
                        "créditos em dinheiro digital — depositados em cartão e aceitos " +
                        "em comércios locais parceiros da cidade.\n\n" +
                        "📋 Como participar\n" +
                        "Leve seus recicláveis ao ecoponto mais próximo e faça o cadastro " +
                        "na primeira entrega. Os créditos entram direto no seu cartão social.\n\n" +
                        "♻️ Aceita: Plástico · Papel · Vidro · Metal · Eletrônicos · Óleo vegetal\n\n" +
                        "🏆 Vencedor do XII Prêmio Sebrae Prefeitura Empreendedora 2024 " +
                        "— Sustentabilidade e Meio Ambiente",
                color           = Color(0xFF1A3A7E),
                titleColor = Color.White,
                url             = "https://www.saogoncalo.rj.gov.br",
                cardDrawable    = R.drawable.programs_btn_recicla_sao_goncalo,
                popupCards      = listOf(R.drawable.programs_card_troque_3),
                showDescription = false,
            ),
        )
    ),

    // ── Coleta Seletiva Municipal ─────────────────────────────────────────────
    ProgramSection(
        title = "Coleta Seletiva Municipal",
        programs = listOf(

            Program(
                name         = "CLIN",
                description  = "A Companhia de Limpeza de Niterói oferece coleta seletiva " +
                        "em toda a cidade desde 1991 — pioneira no estado do Rio de Janeiro.\n\n" +
                        "Modalidades disponíveis\n" +
                        "• Porta a porta — uma vez por semana por bairro\n" +
                        "• PEVs — 9 endereços, dias úteis das 8h às 16h\n" +
                        "• Clin Comunidade Sustentável — contêineres Molok em comunidades\n" +
                        "• Recicla Niterói — conteinerização de rua em Icaraí\n\n" +
                        "♻️ Aceita: Papel · Plástico · Vidro · Metal · Óleo vegetal · Eletrônicos\n" +
                        "📞 (21) 99753-1952",
                color        = Color(0xFFEE7C2D),
                titleColor = Color.White,
                url          = "https://www.clin.rj.gov.br/SiteCLIN/?page_id=575",
                cardDrawable = R.drawable.programs_btn_clin,
                popupCards   = listOf(R.drawable.programs_card_coleta_1),
                showDescription = false,
            ),

            Program(
                name         = "Comlurb",
                description  = "A Comlurb opera a coleta seletiva porta a porta em 117 bairros " +
                        "do Rio de Janeiro — a maior organização de limpeza pública da América Latina.\n\n" +
                        "📋 Como participar\n" +
                        "Separe papel, plástico, vidro e metal do lixo orgânico. " +
                        "Embale em saco transparente ou translúcido — sacos pretos não são aceitos. " +
                        "Coloque na calçada no dia da coleta da sua rua.\n\n" +
                        "♻️ Aceita: Papel · Plástico (PET, PEAD, PP, PVC) · Vidro · Metal\n" +
                        "🗺️ 117 bairros atendidos\n" +
                        "📞 Central 1746",
                color        = Color(0xFFEE7C2D),
                titleColor = Color.White,
                url          = "https://comlurb.prefeitura.rio/servico/coleta-seletiva/historico/",
                cardDrawable = R.drawable.programs_btn_comlurb,
                popupCards   = listOf(R.drawable.programs_card_coleta_2),
                showDescription = false,
            ),

            Program(
                name         = "Jogue Limpo\nDuque de Caxias",
                description  = "O maior programa de limpeza urbana da história de Duque de Caxias, " +
                        "lançado em 2025 com ecopontos para descarte correto nos quatro distritos.\n\n" +
                        "Ecoponto Parque Vila Nova (desde jul/2025)\n" +
                        "Recicláveis coletados são destinados a cooperativas cadastradas. " +
                        "O Centro de Triagem do Jardim Gramacho recicla entulho, madeira e pneus.\n\n" +
                        "♻️ Aceita: Papel · Plástico · Vidro · Metal · Entulho · Pneus · Galhadas\n\n" +
                        "⚠️ Após período educativo, o descarte irregular está sujeito a autuação.",
                color        = Color(0xFF1A3A7E),
                titleColor = Color.White,
                url          = "https://duquedecaxias.rj.gov.br/noticia/programa-jogue-limpo-com-duque-de-caxias-e-lancado/6128",
                cardDrawable = R.drawable.programs_btn_jogue_limpo_duque_de_caxias,
                popupCards   = listOf(R.drawable.programs_card_coleta_3),
                showDescription = false,
            ),

            Program(
                name         = "Coleta Seletiva\nAngra dos Reis",
                description  = "O Programa Municipal de Coleta Seletiva de Angra dos Reis oferece " +
                        "múltiplas formas de descarte no continente e na Ilha Grande.\n\n" +
                        "Modalidades disponíveis\n" +
                        "• Coleta porta a porta — semanal por bairro\n" +
                        "• 14 Ecopontos fixos — espalhados pelo continente e Ilha Grande\n" +
                        "• 12 PEVs itinerantes — dois bairros por dia, Seg–Sex\n\n" +
                        "♻️ Aceita: Papel · Plástico · Vidro · Metal · Óleo vegetal · " +
                        "Eletrônicos · Pneus · Pilhas e baterias\n\n" +
                        "O PEV do Cais do Carmo também recebe e faz o transbordo " +
                        "dos resíduos vindos das ilhas.\n" +
                        "📞 (24) 3377-4402",
                color        = Color(0xFF1A3A7E),
                titleColor = Color.White,
                url          = "https://www.angra.rj.gov.br/servicos/ecopontos-pontos-de-entrega-de-residuos-reciclaveis",
                cardDrawable = R.drawable.programs_btn_coleta_seletiva_angra,
                popupCards   = listOf(R.drawable.programs_card_coleta_4),
                showDescription = false,
            ),
        )
    ),

    // ── Impacto Social e Comunidades ──────────────────────────────────────────
    ProgramSection(
        title = "Impacto Social e Comunidades",
        programs = listOf(

            Program(
                name         = "CLIN",
                description  = "Cada reciclável entregue em Niterói alimenta uma cadeia de trabalho " +
                        "digno. O material coletado pela CLIN vai diretamente para a " +
                        "Cooperativa do Morro do Céu e a Reciclanit — cooperativas de " +
                        "catadores parceiras da companhia.\n\n" +
                        "Os cooperativados fazem a triagem, prensagem e comercialização dos " +
                        "materiais — garantindo renda e melhores condições de trabalho.\n\n" +
                        "👥 Pioneira em coleta seletiva no RJ desde 1991\n" +
                        "♻️ Centenas de famílias beneficiadas",
                color        = Color(0xFFEE7C2D),
                titleColor = Color.White,
                url          = "https://www.clin.rj.gov.br/SiteCLIN/",
                cardDrawable = R.drawable.programs_btn_clin,
                popupCards   = listOf(R.drawable.programs_card_social_1),
                showDescription = false,
            ),

            Program(
                name         = "Light\nRecicla",
                description  = "Prefere ajudar quem precisa? Doe seu bônus para uma das " +
                        "44 instituições sociais cadastradas no programa — escolas, " +
                        "entidades de saúde e de inclusão social espalhadas pelo Rio.\n\n" +
                        "Empresas parceiras podem destinar 100% dos seus créditos " +
                        "diretamente às instituições cadastradas.\n\n" +
                        "🌎 +16.000 clientes beneficiados desde 2011\n" +
                        "♻️ +11.000 toneladas recicladas\n" +
                        "💰 R$ 2,1 milhões em bônus distribuídos\n" +
                        "🏆 Prêmio Ação Ambiental FIRJAN 2014",
                color        = Color(0xFF1C7B6E),
                titleColor = Color.White,
                url          = "https://www.avsibrasil.org.br/projeto/light-recicla/",
                cardDrawable = R.drawable.programs_btn_light_recicla,
                popupCards   = listOf(R.drawable.programs_card_social_2),
                showDescription = false,
            ),

            Program(
                name         = "Comlurb",
                description  = "Todo reciclável coletado pela Comlurb é entregue gratuitamente " +
                        "a 29 cooperativas de catadores, com centrais de triagem em " +
                        "Irajá e Bangu.\n\n" +
                        "Os catadores fazem a triagem, prensagem e comercialização " +
                        "com a indústria da reciclagem — garantindo emprego e renda " +
                        "para centenas de famílias cariocas.\n\n" +
                        "👷 29 cooperativas cadastradas\n" +
                        "👨‍👩‍👧 ~450 famílias beneficiadas\n" +
                        "🏭 Centrais de triagem em Irajá e Bangu",
                color        = Color(0xFFEE7C2D),
                titleColor = Color.White,
                url          = "https://comlurb.prefeitura.rio/servico/coleta-seletiva/cooperativas-de-catadores-cadastradas/",
                cardDrawable = R.drawable.programs_btn_comlurb,
                popupCards   = listOf(R.drawable.programs_card_social_3),
                showDescription = false,
            ),

            Program(
                name         = "Óleo\nSustentável",
                description  = "1 litro de óleo despejado no ralo pode contaminar até " +
                        "25.000 litros de água. O descarte correto é simples e faz " +
                        "toda a diferença.\n\n" +
                        "📋 Como descartar\n" +
                        "① Espere o óleo esfriar completamente\n" +
                        "② Passe por uma peneira para remover resíduos sólidos\n" +
                        "③ Transfira para uma garrafa PET com tampa\n" +
                        "④ Leve a um PEV próximo — use a aba Mapa para encontrar\n\n" +
                        "O óleo coletado é transformado em biodiesel " +
                        "(1 litro de óleo = 1 litro de biodiesel), sabão, tintas ou vernizes.\n\n" +
                        "🌍 +11 milhões de litros coletados no Brasil desde 2012",
                color        = Color(0xFFA88038),
                titleColor = Color.White,
                url          = "https://www.oleosustentavel.org.br/ciclo-do-oleo",
                cardDrawable = R.drawable.programs_btn_oleo_sustentavel,
                popupCards   = listOf(R.drawable.programs_card_social_4),
                showDescription = false,
            ),
        )
    ),
)

// ── Tela ─────────────────────────────────────────────────────────────────────

@Composable
fun ProgramsScreen() {
    var selectedProgram by remember { mutableStateOf<Program?>(null) }
    val popupVisible = selectedProgram != null

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (popupVisible) Modifier.blur(8.dp) else Modifier)
        ) {
            ProgramsScreenContent(
                popupVisible      = popupVisible,
                onProgramSelected = { program -> selectedProgram = program }
            )
        }

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
                    ) { selectedProgram = null },
                contentAlignment = Alignment.Center
            ) {
                selectedProgram?.let { program ->
                    ProgramPopup(
                        program = program,
                        onClose = { selectedProgram = null }
                    )
                }
            }
        }
    }
}

// ── Conteúdo da tela ──────────────────────────────────────────────────────────

@Composable
private fun ProgramsScreenContent(
    popupVisible      : Boolean,
    onProgramSelected : (Program) -> Unit,
) {
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
            painter            = painterResource(R.drawable.programs_art_bottom),
            contentDescription = null,
            contentScale       = ContentScale.FillWidth,
            alignment          = Alignment.BottomCenter,
            modifier           = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
                .offset(y = 10.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {

            Spacer(Modifier.height(67.dp))

            Text(
                text       = "Programas e parcerias",
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth(),
                fontSize   = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
                lineHeight = 20.sp,
                style      = MaterialTheme.typography.headlineLarge
            )

            Spacer(Modifier.height(10.dp))

            PROGRAMS.forEachIndexed { index, section ->
                if (index > 0) Spacer(Modifier.height(24.dp))

                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier  = Modifier.weight(1f),
                        color     = Color.White.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                    Text(
                        text      = section.title,
                        fontSize  = 13.sp,
                        color     = Color.White.copy(alpha = 0.85f),
                        modifier  = Modifier.padding(horizontal = 12.dp),
                        textAlign = TextAlign.Center
                    )
                    HorizontalDivider(
                        modifier  = Modifier.weight(1f),
                        color     = Color.White.copy(alpha = 0.4f),
                        thickness = 1.dp
                    )
                }

                Spacer(Modifier.height(14.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding        = PaddingValues(end = 8.dp)
                ) {
                    items(section.programs) { program ->
                        ProgramCard(
                            program = program,
                            onClick = { if (!popupVisible) onProgramSelected(program) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(220.dp))
        }
    }
}

// ── Card de programa ──────────────────────────────────────────────────────────

@Composable
private fun ProgramCard(
    program : Program,
    onClick : () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed         by interactionSource.collectIsPressedAsState()
    val scale             by animateFloatAsState(
        targetValue   = if (isPressed) 0.94f else 1f,
        animationSpec = tween(if (isPressed) 80 else 160),
        label         = "program_card_scale"
    )

    Box(
        modifier = Modifier
            .size(width = 130.dp, height = 90.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(elevation = 0.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .indication(
                interactionSource = interactionSource,
                indication        = ripple(color = Color.Black.copy(alpha = 0.1f))
            )
            .pointerInput(onClick) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = PressInteraction.Press(offset)
                        interactionSource.emit(press)
                        val released = tryAwaitRelease()
                        if (released) {
                            interactionSource.emit(PressInteraction.Release(press))
                            onClick()
                        } else {
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    }
                )
            }
    ) {
        Image(
            painter            = painterResource(program.cardDrawable),
            contentDescription = program.name,
            contentScale       = ContentScale.FillBounds,
            modifier           = Modifier.fillMaxSize()
        )
    }
}

// ── Popup do programa ─────────────────────────────────────────────────────────

@Composable
private fun ProgramPopup(
    program : Program,
    onClose : () -> Unit,
) {
    val context   = LocalContext.current
    val textColor = program.titleColor
        ?: if (program.color.luminance() > 0.5f) Color(0xFF1A1A1A) else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .fillMaxHeight(0.82f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Cabeçalho colorido ────────────────────────────────────────
            Surface(
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(12.dp),
                color           = program.color,
                shadowElevation = 6.dp
            ) {
                Text(
                    text       = program.name.replace("\n", " "),
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = textColor,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.padding(vertical = 14.dp, horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // ── Descrição textual (opcional por programa) ─────────────────
            if (program.showDescription) {
                Surface(
                    modifier        = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    shape           = RoundedCornerShape(12.dp),
                    color           = Color.White.copy(alpha = 0.93f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text       = program.description,
                        fontSize   = 12.5.sp,
                        color      = Color(0xFF1E1E1E),
                        textAlign  = TextAlign.Start,
                        lineHeight = 18.sp,
                        modifier   = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))
            }

            // ── Card visual informativo ───────────────────────────────────

            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(program.popupCards.first()),
                    contentDescription = program.name,
                    contentScale       = ContentScale.FillWidth,
                    modifier           = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Botões: Fechar + Saiba mais ──────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
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

                Spacer(Modifier.width(16.dp))

                val learnInteraction = remember { MutableInteractionSource() }
                val learnPressed     by learnInteraction.collectIsPressedAsState()
                val learnScale       by animateFloatAsState(
                    targetValue   = if (learnPressed) 0.96f else 1f,
                    animationSpec = tween(if (learnPressed) 80 else 160),
                    label         = "learn_more_btn_scale"
                )

                Surface(
                    modifier = Modifier
                        .height(48.dp)
                        .graphicsLayer { scaleX = learnScale; scaleY = learnScale }
                        .border(width = 2.dp, color = GreenDark, shape = RoundedCornerShape(24.dp))
                        .indication(
                            interactionSource = learnInteraction,
                            indication        = ripple(color = GreenDark.copy(alpha = 0.1f))
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val press = PressInteraction.Press(offset)
                                    learnInteraction.emit(press)
                                    val released = tryAwaitRelease()
                                    if (released) {
                                        learnInteraction.emit(PressInteraction.Release(press))
                                        val intent = Intent(Intent.ACTION_VIEW, program.url.toUri())
                                        context.startActivity(intent)
                                    } else {
                                        learnInteraction.emit(PressInteraction.Cancel(press))
                                    }
                                }
                            )
                        },
                    shape           = RoundedCornerShape(24.dp),
                    color           = Color.White,
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier         = Modifier
                            .padding(horizontal = 28.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "Saiba mais",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = GreenDark
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

/** Fallback de contraste para programas sem titleColor explícito. */
private fun Color.luminance(): Float {
    val r = red.toDouble()
    val g = green.toDouble()
    val b = blue.toDouble()
    return (0.2126 * r + 0.7152 * g + 0.0722 * b).toFloat()
}