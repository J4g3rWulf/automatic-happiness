package com.example.recycleapp.ui.theme
import androidx.compose.ui.graphics.Color
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/*
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)
*/

private val LightColorScheme = lightColorScheme(
    primary = GreenBright,
    secondary = Color.White,
    tertiary = Color.LightGray,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color(0xFF212121),
    onTertiary = Color(0xFF212121),
    onBackground = Color(0xFF212121),
    onSurface = Color(0xFF212121)
)


@Composable
fun RecycleAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme;

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}