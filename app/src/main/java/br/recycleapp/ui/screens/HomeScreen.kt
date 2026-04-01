package br.recycleapp.ui.screens

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import br.recycleapp.R
import br.recycleapp.ui.theme.GreenDark
import br.recycleapp.ui.theme.GreenLight
import br.recycleapp.ui.theme.RecycleAppTheme
import br.recycleapp.ui.theme.WhiteText
import br.recycleapp.ui.viewmodel.HomeAnimationState
import kotlinx.coroutines.delay

/**
 * Tela inicial do RecycleApp.
 *
 * Exibe título, subtítulo, dois botões de ação (câmera e galeria),
 * um card de dica e duas artes decorativas (topo e base).
 *
 * Todos os parâmetros com valores padrão são ajustáveis para
 * afinar o layout sem mexer na lógica — útil para diferentes tamanhos
 * de tela ou ajustes de design futuros.
 *
 * A animação de entrada só roda na primeira abertura do app,
 * controlada por [br.recycleapp.ui.viewmodel.HomeAnimationState].
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,

    // ── Espaçamentos verticais ────────────────────────────────────────
    // Ajuste esses valores para mover os elementos para cima ou para baixo
    titleTop: Dp          = 74.dp,   // distância do topo até o título
    titleToSubtitle: Dp   = 30.dp,   // espaço entre título e subtítulo
    subtitleToButtons: Dp = 24.dp,   // espaço entre subtítulo e botões
    warningTop: Dp        = 40.dp,   // espaço entre botões e card de dica

    // ── Tipografia ────────────────────────────────────────────────────
    titleMaxWidth: Dp      = 353.dp, // largura máxima do bloco de título
    titleLineHeight: Float = 40f,    // altura de linha do título (em sp antes da escala)

    // ── Botões de ação ────────────────────────────────────────────────
    buttonTargetSize: Dp = 167.dp,   // tamanho ideal de cada card quadrado
    buttonCorner: Dp     = 12.dp,    // arredondamento dos cards
    buttonGap: Dp        = 20.dp,    // espaço entre os dois cards
    cameraIconSize: Dp   = 80.dp,    // tamanho do ícone de câmera
    galleryIconSize: Dp  = 70.dp,    // tamanho do ícone de galeria

    // ── Arte decorativa inferior ──────────────────────────────────────
    illustrationHeight: Dp  = 200.dp, // altura fixa da arte (ignora espaço transparente do PNG)
    illustrationOffsetY: Dp = 52.dp,  // desloca a arte para baixo (valores maiores = mais abaixo)

    // ── Layout geral ──────────────────────────────────────────────────
    horizontalPadding: Dp = 20.dp    // padding lateral do conteúdo
) {
    val view = LocalView.current

    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    // ── Escala de largura via WindowSizeClass ─────────────────────────
    // Aplicada a botões e título para aproveitar melhor telas maiores
    val wScale: Float = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1.00f  // celular normal
        WindowWidthSizeClass.Medium  -> 1.10f  // tablet pequeno / foldable
        else                         -> 1.20f  // tablet grande
    }

    // ── Animação de entrada em cascata ────────────────────────────────
    // Cada elemento aparece com fadeIn + deslizamento, em sequência.
    // O flag [HomeAnimationState.hasAnimated] garante que a animação
    // só roda na primeira abertura — retornar para a Home não anima.
    var visible by remember { mutableStateOf(HomeAnimationState.hasAnimated) }
    LaunchedEffect(Unit) {
        if (!HomeAnimationState.hasAnimated) {
            delay(50) // aguarda a splash screen terminar
            visible = true
            HomeAnimationState.markAsAnimated()
        }
    }

    // Cada par (alpha + offsetY) anima um elemento com delay crescente
    val titleAlpha      by animateFloatAsState(if (visible) 1f else 0f, tween(600),      label = "ta")
    val titleOffsetY    by animateFloatAsState(if (visible) 0f else 60f, tween(600),      label = "to")
    val subtitleAlpha   by animateFloatAsState(if (visible) 1f else 0f, tween(600, 150),  label = "sa")
    val subtitleOffsetY by animateFloatAsState(if (visible) 0f else 60f, tween(600, 150), label = "so")
    val buttonsAlpha    by animateFloatAsState(if (visible) 1f else 0f, tween(600, 300),  label = "ba")
    val buttonsOffsetY  by animateFloatAsState(if (visible) 0f else 60f, tween(600, 300), label = "bo")
    val warningAlpha    by animateFloatAsState(if (visible) 1f else 0f, tween(600, 450),  label = "wa")
    val warningOffsetY  by animateFloatAsState(if (visible) 0f else 60f, tween(600, 450), label = "wo")

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background)
        ) {

            // ── Arte decorativa inferior ──────────────────────────────
            // Fica atrás de todo o conteúdo, ancorada na base da tela.
            // A altura fixa evita que o espaço transparente do PNG
            // interfira no posicionamento dos elementos acima.
            Image(
                painter            = painterResource(id = R.drawable.art_bottom_v1),
                contentDescription = null,
                modifier           = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(illustrationHeight)
                    .offset(y = illustrationOffsetY),
                contentScale = ContentScale.FillWidth,
                alignment    = Alignment.BottomCenter
            )

            // ── Arte decorativa superior ──────────────────────────────
            // Fica atrás do título, criando o padrão visual de ondas
            // no topo da tela. Não tem offset — sempre no topo.
            Image(
                painter            = painterResource(id = R.drawable.art_top_v1),
                contentDescription = null,
                modifier           = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                alignment    = Alignment.TopCenter
            )

            // ── Conteúdo principal ────────────────────────────────────
            // BoxWithConstraints expõe maxWidth/maxHeight para o cálculo
            // de dimensões responsivas em HomeScreenDimensions.kt
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = horizontalPadding)
            ) {
                val dims = rememberHomeScreenDimensions(
                    boxMaxW          = maxWidth,
                    boxMaxH          = maxHeight,
                    wScale           = wScale,
                    titleTop         = titleTop,
                    buttonTargetSize = buttonTargetSize,
                    buttonGap        = buttonGap
                )

                Column(
                    modifier            = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {

                    Spacer(Modifier.height(dims.titleTopEff))

                    // Título principal
                    Text(
                        text  = stringResource(R.string.title_home),
                        color = WhiteText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize   = (30f * dims.scaleForTitle).sp,
                            lineHeight = (titleLineHeight * dims.scaleForTitle).sp
                        ),
                        modifier = Modifier
                            .padding(start = dims.leftInset)
                            .widthIn(max = dims.pairWidth.coerceAtMost(titleMaxWidth))
                            .graphicsLayer {
                                alpha        = titleAlpha
                                translationY = titleOffsetY
                            }
                    )

                    Spacer(Modifier.height(titleToSubtitle))

                    // Subtítulo — instrução resumida para o usuário
                    Text(
                        text      = stringResource(R.string.btn_subtitle_home),
                        color     = WhiteText,
                        style     = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha        = subtitleAlpha
                                translationY = subtitleOffsetY
                            }
                    )

                    Spacer(Modifier.height(subtitleToButtons))

                    // Botões de ação: câmera (primário) e galeria (secundário)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha        = buttonsAlpha
                                translationY = buttonsOffsetY
                            },
                        horizontalArrangement = Arrangement.spacedBy(
                            buttonGap, alignment = Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionButtonWithLabel(
                            title       = stringResource(R.string.btn_camera),
                            size        = dims.cardSize,
                            corner      = buttonCorner,
                            container   = MaterialTheme.colorScheme.primaryContainer,
                            iconPainter = painterResource(R.drawable.ic_camera),
                            iconTint    = MaterialTheme.colorScheme.onPrimaryContainer,
                            iconSize    = (cameraIconSize.value * dims.scaleForButtons).dp,
                            onClick     = onOpenCamera
                        )
                        ActionButtonWithLabel(
                            title       = stringResource(R.string.btn_gallery),
                            size        = dims.cardSize,
                            corner      = buttonCorner,
                            container   = MaterialTheme.colorScheme.secondaryContainer,
                            iconPainter = painterResource(R.drawable.ic_gallery),
                            iconTint    = MaterialTheme.colorScheme.onSecondaryContainer,
                            iconSize    = (galleryIconSize.value * dims.scaleForButtons).dp,
                            onClick     = onOpenGallery
                        )
                    }

                    Spacer(Modifier.height(warningTop))

                    // Card de dica - orienta o usuário sobre como tirar a foto
                    Row(
                        modifier = Modifier
                            .padding(start = dims.leftInset)
                            .width(dims.pairWidth)
                            .shadow(6.dp, RoundedCornerShape(10.dp))
                            .background(color = WhiteText, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .graphicsLayer {
                                alpha        = warningAlpha
                                translationY = warningOffsetY
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ícone circular de aviso
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(GreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter            = painterResource(R.drawable.ic_warning),
                                contentDescription = "Aviso",
                                tint               = GreenDark,
                                modifier           = Modifier.size(14.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        // Texto da dica - máximo 2 linhas
                        Text(
                            text  = stringResource(R.string.notice_text),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize   = (14f * dims.noticeTextScale).sp,
                                lineHeight = (20f * dims.noticeTextScale).sp
                            ),
                            maxLines = 2,
                            color    = GreenDark
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Componentes privados - usados apenas dentro desta tela
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Card quadrado com ícone centralizado e rótulo de texto abaixo.
 * Usado para os botões de câmera e galeria.
 */
@Composable
private fun ActionButtonWithLabel(
    title: String,
    size: Dp,
    corner: Dp,
    container: Color,
    iconPainter: Painter,
    iconTint: Color,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    Column(
        modifier            = Modifier.width(size),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ActionSquareOnlyIcon(
            size        = size,
            corner      = corner,
            container   = container,
            iconPainter = iconPainter,
            iconTint    = iconTint,
            iconSize    = iconSize,
            onClick     = onClick
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text      = title,
            style     = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Card quadrado elevado contendo apenas um ícone centralizado.
 * Responsável pelo visual e pelo clique - o rótulo fica em [ActionButtonWithLabel].
 */
@Composable
private fun ActionSquareOnlyIcon(
    size: Dp,
    corner: Dp,
    container: Color,
    iconPainter: Painter,
    iconTint: Color,
    iconSize: Dp,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick   = onClick,
        shape     = RoundedCornerShape(corner),
        colors    = CardDefaults.elevatedCardColors(containerColor = container),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        modifier  = Modifier.size(size)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter            = iconPainter,
                contentDescription = null,
                tint               = iconTint,
                modifier           = Modifier.size(iconSize)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews - visíveis no painel de Preview do Android Studio
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Compact - Pixel 5", device = Devices.PIXEL_5)
@Composable
private fun HomeScreenPreviewCompact() {
    RecycleAppTheme {
        HomeScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                androidx.compose.ui.unit.DpSize(360.dp, 780.dp)
            ),
            onOpenCamera  = {},
            onOpenGallery = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Medium - Tablet pequeno", widthDp = 700, heightDp = 900)
@Composable
private fun HomeScreenPreviewMedium() {
    RecycleAppTheme {
        HomeScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                androidx.compose.ui.unit.DpSize(700.dp, 900.dp)
            ),
            onOpenCamera  = {},
            onOpenGallery = {}
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Expanded - Tablet", widthDp = 1000, heightDp = 800)
@Composable
private fun HomeScreenPreviewExpanded() {
    RecycleAppTheme {
        HomeScreen(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                androidx.compose.ui.unit.DpSize(1000.dp, 800.dp)
            ),
            onOpenCamera  = {},
            onOpenGallery = {}
        )
    }
}