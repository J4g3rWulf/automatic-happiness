package br.recycleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import br.recycleapp.ui.navigation.AppNavHost
import br.recycleapp.ui.theme.RecycleAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activity principal do aplicativo RecycleApp.
 *
 * Configura a splash screen, edge-to-edge UI e o host de navegação.
 * Calcula o WindowSizeClass para suportar layouts responsivos em diferentes tamanhos de tela.
 */
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Exibe splash screen por 1.5s apenas no primeiro launch (não em rotação)
        val shouldDelay = savedInstanceState == null
        var keep = shouldDelay
        splash.setKeepOnScreenCondition { keep }
        if (shouldDelay) {
            lifecycleScope.launch {
                delay(1500)
                keep = false
            }
        }

        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            RecycleAppTheme {
                AppNavHost(windowSizeClass = windowSizeClass)
            }
        }
    }
}