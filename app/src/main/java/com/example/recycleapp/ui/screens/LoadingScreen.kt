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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.recycleapp.R
import com.example.recycleapp.ui.theme.GreenDark
import com.example.recycleapp.ui.theme.GreenPrimary
import com.example.recycleapp.ui.theme.WhiteText
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(
    photoUri: String,
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    BackHandler { onBack() }

    // Label fake enquanto a API não existe
    val fakeLabel = "Plástico"

    // ===== Navegação automática após alguns segundos =====
    LaunchedEffect(photoUri) {
        // Dura aprox. 1 volta completa do círculo (mesmo tempo da animação)
        delay(4200)
        onResult(fakeLabel)
    }

    // Animação de rotação do círculo (mais devagar)
    val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
    val rotation = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4200, // você já ajustou pra esse valor
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading_rotation_angle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Fundo verde com ~21% de opacidade
            .background(GreenPrimary.copy(alpha = 0.21f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .offset(y = 24.dp)
        ) {
            // Círculo de loading + ícone central
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Círculo girando
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

                // Ícone central (vector asset do Figma)
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
                            // troque o nome acima se seu vector tiver outro nome
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
                style = MaterialTheme.typography.bodyMedium, // Poppins
                color = GreenDark,
                textAlign = TextAlign.Center
            )
        }
    }
}
