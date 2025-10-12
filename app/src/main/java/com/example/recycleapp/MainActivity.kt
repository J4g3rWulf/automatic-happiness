package com.example.recycleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.recycleapp.navigation.AppNavHost
import com.example.recycleapp.ui.theme.RecycleAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // instala a splash do sistema ANTES do super.onCreate
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // só aplica delay se for o 1º launch (não em recriações)
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
            RecycleAppTheme {
                AppNavHost() // startDestination = Home
            }
        }
    }
}