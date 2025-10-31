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

    // ======= CONTROLES VISUAIS =======
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
    horizontalPadding: Dp = 20.dp
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ===== FUNDO =====
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

            // ===== CONTEÚDO =====
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = horizontalPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(Modifier.height(titleTop))

                Text(
                    text = "Identifique o tipo\nde lixo assim:",
                    color = WhiteText,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        lineHeight = titleLineHeight.sp
                    ),
                    modifier = Modifier.widthIn(max = titleMaxWidth)
                )

                Spacer(Modifier.height(titleToButtons))

                // ===== Botões =====
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val cardSize: Dp = ((maxWidth - buttonGap) / 2).coerceAtMost(buttonTargetSize)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonGap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionButtonWithLabel(
                            title = "Tire uma foto",
                            size = cardSize,
                            corner = buttonCorner,
                            container = MaterialTheme.colorScheme.primaryContainer,
                            iconPainter = painterResource(R.drawable.ic_camera),
                            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                            iconSize = cameraIconSize,
                            onClick = onOpenCamera
                        )
                        ActionButtonWithLabel(
                            title = "Use da galeria",
                            size = cardSize,
                            corner = buttonCorner,
                            container = MaterialTheme.colorScheme.secondaryContainer,
                            iconPainter = painterResource(R.drawable.ic_gallery),
                            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                            iconSize = galleryIconSize,
                            onClick = onOpenGallery
                        )
                    }
                }

                Spacer(Modifier.height(warningTop))

                // ===== Aviso =====
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.Start)
                        .padding(horizontal = 4.dp)
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
                    Text(
                        text = "Certifique-se que o recipiente não possui nenhum resíduo!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenDark
                    )
                }

                Spacer(Modifier.weight(1f))
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
