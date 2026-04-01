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

        // Restaura a barra ANTES de navegar para a Home
        WindowCompat.getInsetsController(
            (view.context as Activity).window,
            view
        ).show(WindowInsetsCompat.Type.navigationBars())

        delay(100)  // tempo entre a barra aparecer e a Home abrir

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