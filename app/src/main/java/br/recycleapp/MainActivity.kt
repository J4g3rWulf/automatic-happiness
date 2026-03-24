package br.recycleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import br.recycleapp.navigation.AppNavHost
import br.recycleapp.ui.theme.RecycleAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

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
            // Calcula o tamanho da janela uma única vez aqui
            // e passa para todas as telas via AppNavHost
            val windowSizeClass = calculateWindowSizeClass(this)

            RecycleAppTheme {
                AppNavHost(windowSizeClass = windowSizeClass)
            }
        }
    }
}