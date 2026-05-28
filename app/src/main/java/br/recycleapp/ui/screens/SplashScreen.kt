package br.recycleapp.ui.screens

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import br.recycleapp.R
import kotlinx.coroutines.delay

private val SplashBackground = Color(0xFFF6F5FA)

/**
 * Splash screen customizada com logo animado em três fases:
 * pausa inicial → fade-in do logo → fade-out → [onSplashFinished].
 *
 * Força a cor da barra de navegação para corresponder ao fundo claro da splash
 * e oculta a barra durante a exibição. A splash nativa do sistema é instalada
 * em [MainActivity] via `installSplashScreen()`; esta tela é a camada Compose
 * que a sucede com a animação do logo.
 *
 * @param onSplashFinished chamado ao término da animação para navegar para a Home
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val alpha = remember { Animatable(0f) }
    val view = LocalView.current

    // Força nav bar com a cor do fundo da splash
    @Suppress("DEPRECATION")
    SideEffect {
        val window = (view.context as Activity).window
        window.navigationBarColor = ContextCompat.getColor(view.context, R.color.splash_background)
        WindowCompat.getInsetsController(window, view).apply {
            hide(WindowInsetsCompat.Type.navigationBars()) // esconde a barra completamente
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE // volta ao deslizar
        }
    }

    LaunchedEffect(Unit) {
        // Pausa inicial - tela limpa sem logo
        delay(1000)

        // Fase 1 - surge do fundo (fade-in lento)
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )

        // Fase 2 - logo visível
        delay(2500)

        // Fase 3 - dissolve de volta ao fundo (fade-out)
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 800)
        )

        delay(100)

        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo_v2),
            contentDescription = "RecycleApp",
            modifier = Modifier
                .width(260.dp)
                .alpha(alpha.value)
        )
    }
}