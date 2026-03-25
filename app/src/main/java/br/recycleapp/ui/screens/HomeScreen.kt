package br.recycleapp.ui.screens

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.recycleapp.R
import br.recycleapp.ui.theme.GreenDark
import br.recycleapp.ui.theme.GreenLight
import br.recycleapp.ui.theme.RecycleAppTheme
import br.recycleapp.ui.theme.WhiteText
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    // Espaçamentos — ajuste aqui para mover elementos verticalmente
    titleTop: Dp        = 74.dp,
    titleMaxWidth: Dp   = 353.dp,
    titleLineHeight: Float = 40f,
    titleToSubtitle: Dp = 30.dp,
    subtitleToButtons: Dp = 24.dp,
    warningTop: Dp      = 40.dp,
    // Botões
    buttonTargetSize: Dp = 167.dp,
    buttonCorner: Dp     = 12.dp,
    buttonGap: Dp        = 20.dp,
    cameraIconSize: Dp   = 80.dp,
    galleryIconSize: Dp  = 70.dp,
    // Arte inferior
    illustrationOffsetY: Dp  = 52.dp,
    illustrationHeight: Dp   = 200.dp,
    horizontalPadding: Dp    = 20.dp,
    bottomGuardFactor: Float = 0.10f
) {
    // Escala por tamanho de janela
    val wScale: Float = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1.00f
        WindowWidthSizeClass.Medium  -> 1.10f
        else                         -> 1.20f
    }

    // Animação de entrada — só roda na primeira abertura do app
    var visible by remember { mutableStateOf(HomeAnimationState.hasAnimated) }
    LaunchedEffect(Unit) {
        if (!HomeAnimationState.hasAnimated) {
            delay(1100)
            visible = true
            HomeAnimationState.hasAnimated = true
        }
    }

    val titleAlpha      by animateFloatAsState(if (visible) 1f else 0f, tween(600),       label = "ta")
    val titleOffsetY    by animateFloatAsState(if (visible) 0f else 60f, tween(600),       label = "to")
    val subtitleAlpha   by animateFloatAsState(if (visible) 1f else 0f, tween(600, 150),   label = "sa")
    val subtitleOffsetY by animateFloatAsState(if (visible) 0f else 60f, tween(600, 150),  label = "so")
    val buttonsAlpha    by animateFloatAsState(if (visible) 1f else 0f, tween(600, 300),   label = "ba")
    val buttonsOffsetY  by animateFloatAsState(if (visible) 0f else 60f, tween(600, 300),  label = "bo")
    val warningAlpha    by animateFloatAsState(if (visible) 1f else 0f, tween(600, 450),   label = "wa")
    val warningOffsetY  by animateFloatAsState(if (visible) 0f else 60f, tween(600, 450),  label = "wo")

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Arte inferior
            val bottomPainter = painterResource(id = R.drawable.bottom_art)
            val aspectRatio = remember(bottomPainter) {
                val s = bottomPainter.intrinsicSize
                if (s.width > 0 && s.height > 0) s.height / s.width else (9f / 16f)
            }
            Image(
                painter            = bottomPainter,
                contentDescription = null,
                modifier           = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(illustrationHeight)
                    .offset(y = illustrationOffsetY),
                contentScale = ContentScale.FillWidth,
                alignment    = Alignment.BottomCenter
            )

            // Arte superior
            Image(
                painter            = painterResource(id = R.drawable.top_art),
                contentDescription = null,
                modifier           = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                alignment    = Alignment.TopCenter
            )

            // Conteúdo principal
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = horizontalPadding)
            ) {
                // Cálculos de dimensão extraídos para HomeScreenDimensions.kt
                val dims = rememberHomeScreenDimensions(
                    boxMaxW          = maxWidth,
                    boxMaxH          = maxHeight,
                    wScale           = wScale,
                    titleTop         = titleTop,
                    buttonTargetSize = buttonTargetSize,
                    buttonGap        = buttonGap,
                    warningTop       = warningTop,
                    bottomGuardFactor = bottomGuardFactor,
                    aspectRatio      = aspectRatio
                )

                Column(
                    modifier            = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(Modifier.height(dims.titleTopEff))

                    // Título
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

                    // Subtítulo
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

                    // Botões
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

                    Spacer(Modifier.height(dims.warningTopEff))

                    // Card de dica
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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

// ===== Previews =====

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(name = "Compact — Pixel 5", device = Devices.PIXEL_5)
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
@Preview(name = "Medium — Tablet pequeno", widthDp = 700, heightDp = 900)
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
@Preview(name = "Expanded — Tablet", widthDp = 1000, heightDp = 800)
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