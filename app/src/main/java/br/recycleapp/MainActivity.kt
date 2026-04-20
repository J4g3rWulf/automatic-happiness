package br.recycleapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import br.recycleapp.ui.navigation.AppNavHost
import br.recycleapp.ui.theme.RecycleAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instala a Splash Screen da API do Android
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Lógica de retenção da splash screen nativa
        var keep = true
        splash.setKeepOnScreenCondition { keep }
        lifecycleScope.launch {
            delay(1)
            keep = false
        }

        // Configura o Edge-to-Edge (desenha por baixo das barras do sistema)
        enableEdgeToEdge()

        // Remove o scrim (sombra) translúcido forçado pelo sistema na barra de navegação
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            RecycleAppTheme {
                AppNavHost(windowSizeClass = windowSizeClass)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemNavigation()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemNavigation()
        }
    }

    /**
     * Aplica o modo imersivo utilizando apenas a API moderna (WindowInsetsController).
     * Removemos as flags de sistema legadas que causavam conflito com o Edge-to-Edge
     * e provocavam saltos no layout.
     */
    private fun hideSystemNavigation() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.apply {
            // Permite que as barras apareçam temporariamente ao deslizar (transient)
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            // Esconde apenas a barra de navegação (botões/gestos)
            hide(WindowInsetsCompat.Type.navigationBars())
        }
    }
}