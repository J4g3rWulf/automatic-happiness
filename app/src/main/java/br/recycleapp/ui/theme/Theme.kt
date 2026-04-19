package br.recycleapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// AppTypography agora vem do Type.kt - sem definição inline

private val LightColorScheme = lightColorScheme(
    primary              = GreenPrimary,
    onPrimary            = WhiteText,
    primaryContainer     = GreenButton,      // fundo botão câmera
    onPrimaryContainer   = GreenButtonIcon,  // ícone botão câmera
    secondaryContainer   = GreenButton,      // fundo botão galeria
    onSecondaryContainer = GreenButtonIcon,  // ícone botão galeria
    background           = GreenPrimary,
    onBackground         = WhiteText,
    surface              = GreenPrimary,
    onSurface            = WhiteText
)

private val DarkColorScheme = darkColorScheme(
    primary              = GreenPrimary,
    onPrimary            = Color.White,
    primaryContainer     = GreenButton,
    onPrimaryContainer   = GreenButtonIcon,
    secondaryContainer   = GreenButton,
    onSecondaryContainer = GreenButtonIcon,
    background           = BackgroundDark,
    onBackground         = OnSurfaceDark,
    surface              = SurfaceDark,
    onSurface            = OnSurfaceDark
)

@Composable
fun RecycleAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}