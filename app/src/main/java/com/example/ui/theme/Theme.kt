package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val AppleGrayLight = Color(0xFF8E8E93)

val AppleShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val OledDarkColorScheme = darkColorScheme(

    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = Color.White,
    secondary = AppleGrayLight,
    onSecondary = Color.White,
    background = OledBlack,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkGlassRim
)

private val AppleLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = LightSurface,
    onPrimaryContainer = Color.Black,
    secondary = AppleGrayLight,
    onSecondary = Color.Black,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0x1F000000)
)

@Composable
fun SalimTheme(
    darkTheme: Boolean = true,
    isOledBlack: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        if (isOledBlack) OledDarkColorScheme else OledDarkColorScheme.copy(background = Color(0xFF101014))
    } else {
        AppleLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppleShapes,
        content = content
    )
}

