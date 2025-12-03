package com.example.recycleapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recycleapp.R
import com.example.recycleapp.ui.theme.GreenDark
import com.example.recycleapp.ui.theme.GreenPrimary
import com.example.recycleapp.ui.theme.WhiteText
import com.example.recycleapp.util.TrashClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// Tempo mínimo de exibição da tela de loading (efeito só visual/UX)
private const val MIN_LOADING_MS = 1800L

@Composable
fun LoadingScreen(
    photoUri: String,
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current

    // Reutiliza a mesma instância do modelo enquanto a tela estiver na árvore
    val classifier = remember { TrashClassifier(context) }

    BackHandler { onBack() }

    // Classificação em background + garantia de tempo mínimo na tela
    LaunchedEffect(photoUri) {
        val start = System.currentTimeMillis()

        // Roda o TFLite fora da main thread
        val material = withContext(Dispatchers.Default) {
            classifier.classifyMaterial(photoUri)
        }

        val elapsed = System.currentTimeMillis() - start
        if (elapsed < MIN_LOADING_MS) {
            delay(MIN_LOADING_MS - elapsed)
        }

        onResult(material)
    }

    // Animação do círculo de loading
    val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation_angle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenPrimary.copy(alpha = 0.21f)),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .offset(y = 24.dp)
        ) {
            val maxW = maxWidth

            // Ajuste leve de fonte em telas bem estreitas
            val isNarrow = maxW < 340.dp
            val baseTextStyle = MaterialTheme.typography.bodyMedium
            val textScale = if (isNarrow) 0.92f else 1f

            val loadingTextStyle = baseTextStyle.copy(
                fontSize = (baseTextStyle.fontSize.value * textScale).sp,
                lineHeight = (baseTextStyle.lineHeight.value * textScale).sp
            )

            // Limita a largura do texto para controlar melhor a quebra de linha
            val textMaxWidth = maxW.coerceAtMost(360.dp)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                // Círculo de loading + ícone central
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationZ = rotation.value }
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 14.dp,
                            color = GreenPrimary
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),
                        color = WhiteText.copy(alpha = 0.9f),
                        shadowElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_recycle_loading),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.loading_message),
                    style = loadingTextStyle,
                    color = GreenDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 8.dp)
                        .widthIn(max = textMaxWidth)
                )
            }
        }
    }
}