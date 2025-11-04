package com.example.recycleapp.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.recycleapp.R

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = WhiteText,

    primaryContainer = GreenLight,
    onPrimaryContainer = GreenInk,

    secondaryContainer = GreenDark,
    onSecondaryContainer = GreenLight,

    background = GreenPrimary,
    onBackground = WhiteText,
    surface = GreenPrimary,
    onSurface = WhiteText
)

/*private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = WhiteText,
    background = Color(0xFF0E0F0E),
    onBackground = WhiteText,
)*/

// ====== Font Poppins ======
private val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    // Font(R.font.poppins_medium, FontWeight.Medium),
    // Font(R.font.poppins_bold, FontWeight.Bold),
)

// ====== Typography ======
val AppTypography = androidx.compose.material3.Typography(
    headlineLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 35.sp,
        lineHeight = 48.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    )
)

@Composable
fun RecycleAppTheme(
    //darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme;

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}