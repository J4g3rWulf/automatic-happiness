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
    primaryContainer     = GreenLight,
    onPrimaryContainer   = GreenInk,
    secondaryContainer   = GreenDark,
    onSecondaryContainer = GreenLight,
    background           = GreenPrimary,
    onBackground         = WhiteText,
    surface              = GreenPrimary,
    onSurface            = WhiteText
)

private val DarkColorScheme = darkColorScheme(
    primary              = GreenPrimary,
    onPrimary            = Color.White,
    primaryContainer     = GreenDark,
    onPrimaryContainer   = GreenLight,
    secondaryContainer   = GreenDark,
    onSecondaryContainer = GreenLight,
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