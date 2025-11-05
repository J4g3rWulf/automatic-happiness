package com.example.recycleapp.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recycleapp.R
import com.example.recycleapp.ui.theme.GreenDark
import com.example.recycleapp.ui.theme.GreenLight
import com.example.recycleapp.ui.theme.WhiteText

@Composable
fun HomeScreen(
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit,

    // ---- Controles visuais (ajustes rápidos sem mexer no layout) ----
    titleTop: Dp = 60.dp,
    titleMaxWidth: Dp = 353.dp,
    titleLineHeight: Float = 48f,
    titleToButtons: Dp = 45.dp,

    buttonTargetSize: Dp = 167.dp,
    buttonCorner: Dp = 12.dp,
    buttonGap: Dp = 20.dp,

    cameraIconSize: Dp = 80.dp,
    galleryIconSize: Dp = 70.dp,

    warningTop: Dp = 50.dp,

    illustrationOffsetY: Dp = 48.dp,
    horizontalPadding: Dp = 20.dp,

    // Fração de “folga” reservada acima da ilustração de fundo
    bottomGuardFactor: Float = 0.30f
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background)
        ) {

            // ============= Camada de fundo (ilustração) ============
            val bottomPainter = painterResource(id = R.drawable.home_illustration)
            val aspectRatio = remember(bottomPainter) {
                val s = bottomPainter.intrinsicSize
                if (s.width > 0 && s.height > 0) s.height / s.width else (9f / 16f)
            }
            Image(
                painter = bottomPainter,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .navigationBarsPadding()
                    .offset(y = illustrationOffsetY),
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.BottomCenter
            )

            // ============= Conteúdo (título, botões e aviso) ============
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = horizontalPadding)
            ) {
                val boxMaxW = this.maxWidth
                val boxMaxH = this.maxHeight

                // Breakpoints de altura para evitar rolagem em telas menores
                val bpSmallH = 700.dp
                val bpTinyH  = 630.dp
                val hScale: Float = when {
                    boxMaxH < bpTinyH  -> 0.80f
                    boxMaxH < bpSmallH -> 0.90f
                    else               -> 1.00f
                }
                val isSmallH = hScale < 1f

                // Breakpoints de largura: crescendo progressivo em telas maiores
                val wScale: Float = when {
                    boxMaxW > 500.dp -> 1.20f
                    boxMaxW > 440.dp -> 1.10f
                    boxMaxW > 390.dp -> 1.05f
                    else             -> 1.00f
                }

                // Escalas separadas: título e botões têm políticas diferentes
                val scaleForButtons = hScale * wScale
                val scaleForTitle   = if (isSmallH) 0.95f else wScale

                // Espaçamentos derivados da escala vertical
                val titleTopEff       = titleTop       * hScale
                val titleToButtonsEff = titleToButtons * hScale
                // Aviso: em telas realmente baixas, mantemos um valor fixo mais curto
                val warningTopEff     = if (isSmallH) 25.dp else warningTop

                // Dimensão do par de botões e recuo esquerdo para alinhar título/aviso
                val effectiveTarget = buttonTargetSize * scaleForButtons
                val cardSize: Dp = ((boxMaxW - buttonGap) / 2).coerceAtMost(effectiveTarget)
                val pairWidth = (cardSize * 2 + buttonGap)     // largura total do par
                val leftInset = (boxMaxW - pairWidth) / 2      // mesmo recuo do primeiro botão

                // Texto do aviso: escala adaptativa para caber em 2 linhas na largura dos botões
                val noticeTextScale = when {
                    pairWidth < 280.dp -> 0.86f
                    pairWidth < 320.dp -> 0.92f
                    else               -> 1.00f
                }

                // Reserva de espaço sobre a ilustração considerando a altura efetiva dela
                val illusHeight = boxMaxW / aspectRatio
                val guardFactor = when {
                    hScale <= 0.80f -> bottomGuardFactor * 0.55f
                    hScale <  1.00f -> bottomGuardFactor * 0.70f
                    else            -> bottomGuardFactor
                }
                val contentBottomPadding = illusHeight * guardFactor

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = contentBottomPadding),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Spacer(Modifier.height(titleTopEff))

                    // Título: escala de tamanho + lineHeight, largura limitada ao par de botões
                    val baseTitle = MaterialTheme.typography.headlineLarge
                    val scaledTitle = baseTitle.copy(
                        fontSize  = (baseTitle.fontSize.value * scaleForTitle).sp,
                        lineHeight = (titleLineHeight * scaleForTitle).sp
                    )
                    val titleMax = if (titleMaxWidth < pairWidth) titleMaxWidth else pairWidth

                    Text(
                        text = stringResource(R.string.title_home),
                        color = WhiteText,
                        style = scaledTitle,
                        modifier = Modifier
                            .padding(start = leftInset)     // alinhado ao 1º botão
                            .widthIn(max = titleMax)
                    )

                    Spacer(Modifier.height(titleToButtonsEff))

                    // Botões: par centralizado, tamanho proporcional ao contexto
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            buttonGap,
                            alignment = Alignment.CenterHorizontally
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionButtonWithLabel(
                            title = stringResource(R.string.btn_camera),
                            size = cardSize,
                            corner = buttonCorner,
                            container = MaterialTheme.colorScheme.primaryContainer,
                            iconPainter = painterResource(R.drawable.ic_camera),
                            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                            iconSize = (cameraIconSize.value * scaleForButtons).dp,
                            onClick = onOpenCamera
                        )
                        ActionButtonWithLabel(
                            title = stringResource(R.string.btn_gallery),
                            size = cardSize,
                            corner = buttonCorner,
                            container = MaterialTheme.colorScheme.secondaryContainer,
                            iconPainter = painterResource(R.drawable.ic_gallery),
                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                            iconSize = (galleryIconSize.value * scaleForButtons).dp,
                            onClick = onOpenGallery
                        )
                    }

                    Spacer(Modifier.height(warningTopEff))

                    // Aviso: mesma largura do par de botões; texto limitado a 2 linhas
                    Row(
                        modifier = Modifier
                            .padding(start = leftInset)
                            .width(pairWidth)
                            .shadow(6.dp, RoundedCornerShape(10.dp))
                            .background(color = WhiteText, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                painter = painterResource(R.drawable.ic_warning),
                                contentDescription = "Aviso",
                                tint = GreenDark,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))

                        val baseBody = MaterialTheme.typography.bodyMedium
                        Text(
                            text = stringResource(R.string.notice_text),
                            style = baseBody.copy(
                                fontSize  = (baseBody.fontSize.value  * noticeTextScale).sp,
                                lineHeight = (baseBody.lineHeight.value * noticeTextScale).sp
                            ),
                            maxLines = 2,
                            color = GreenDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonWithLabel(
    title: String,
    size: Dp,
    corner: Dp,
    container: androidx.compose.ui.graphics.Color,
    iconPainter: Painter,
    iconTint: androidx.compose.ui.graphics.Color,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.width(size), horizontalAlignment = Alignment.CenterHorizontally) {
        ActionSquareOnlyIcon(
            size = size,
            corner = corner,
            container = container,
            iconPainter = iconPainter,
            iconTint = iconTint,
            iconSize = iconSize,
            onClick = onClick
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ActionSquareOnlyIcon(
    size: Dp,
    corner: Dp,
    container: androidx.compose.ui.graphics.Color,
    iconPainter: Painter,
    iconTint: androidx.compose.ui.graphics.Color,
    iconSize: Dp,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.elevatedCardColors(containerColor = container),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        modifier = Modifier.size(size)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
