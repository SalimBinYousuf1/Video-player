package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppleSmokeBlue
import com.example.ui.theme.AppleSmokeGreen
import com.example.ui.theme.AppleSmokeOrange
import com.example.ui.theme.AppleSmokePink
import com.example.ui.theme.AppleSmokePurple
import com.example.ui.theme.AppleSmokeTeal
import com.example.ui.theme.DarkGlassBase
import com.example.ui.theme.DarkGlassRim
import com.example.ui.theme.GoogleSmokeBlue
import com.example.ui.theme.GoogleSmokeGreen
import com.example.ui.theme.GoogleSmokeRed
import com.example.ui.theme.GoogleSmokeYellow
import com.example.ui.theme.LightGlassBase
import com.example.ui.theme.LightGlassRim
import com.example.ui.theme.SpecularHighlight
import kotlin.math.cos
import kotlin.math.sin

/**
 * Continuous squircle corner shape matching Apple standard corner smoothing.
 */
fun squircleShape(radius: Dp): RoundedCornerShape = RoundedCornerShape(radius)

/**
 * Apple Liquid Glass Material Surface with specular rim highlight,
 * physical depth, darkened perimeter border, and configurable transparency.
 * Adapts seamlessly to Light and Dark mode.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    transparencyAlpha: Float = 0.65f,
    isHighContrast: Boolean = false,
    isLightMode: Boolean = false,
    contentAlignment: Alignment = Alignment.Center,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val effectiveAlpha = if (isHighContrast) 0.96f else transparencyAlpha.coerceIn(0.2f, 0.95f)

    val surfaceBrush = if (isLightMode) {
        Brush.verticalGradient(
            colors = listOf(
                LightGlassBase.copy(alpha = (effectiveAlpha * 1.1f).coerceAtMost(0.98f)),
                LightGlassBase.copy(alpha = effectiveAlpha)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                DarkGlassBase.copy(alpha = (effectiveAlpha * 1.15f).coerceAtMost(0.98f)),
                DarkGlassBase.copy(alpha = effectiveAlpha)
            )
        )
    }

    val rimBrush = if (isLightMode) {
        Brush.verticalGradient(
            colors = listOf(
                LightGlassRim.copy(alpha = if (isHighContrast) 0.9f else 0.7f),
                Color(0x22000000),
                Color(0x0A000000)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                SpecularHighlight.copy(alpha = if (isHighContrast) 0.8f else 0.45f),
                DarkGlassRim.copy(alpha = if (isHighContrast) 0.6f else 0.25f),
                Color(0x0A000000)
            )
        )
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = if (isLightMode) Color(0x33000000) else Color.White),
            onClick = onClick
        )
    } else {
        Modifier
    }

    val shadowModifier = if (isLightMode) {
        Modifier.shadow(elevation = 6.dp, shape = shape, spotColor = Color(0x1A000000), ambientColor = Color(0x0D000000))
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .background(surfaceBrush)
            .border(width = 1.dp, brush = rimBrush, shape = shape)
            .then(clickModifier),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * Smooth ambient background colored gradient smoke moving like snow / fog gently colored.
 * Supports:
 * - Clean Apple White background (#FFFFFF) with blue, green, and orange fog drifting gently (default Light Mode)
 * - Deep Black OLED background with ethereal chromatic glowing fog (Dark Mode)
 * - Selectable accent themes: Blue-Green-Orange, Google Spectrum, Apple Sunset, Aurora
 */
@Composable
fun LiquidSmokeBackground(
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false,
    accentTheme: String = "blue_green_orange",
    alphaMultiplier: Float = 0.45f,
    speedMultiplier: Float = 1.0f,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "smoke_motion")
    val durationBase = (18000 / speedMultiplier.coerceIn(0.5f, 2.5f)).toInt()

    val phase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationBase, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (durationBase * 1.35f).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val snowDrift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (durationBase * 0.7f).toInt(), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "snowDrift"
    )

    // Select color palette based on accent theme
    val (color1, color2, color3, color4) = when (accentTheme) {
        "google" -> Quad(GoogleSmokeBlue, GoogleSmokeRed, GoogleSmokeYellow, GoogleSmokeGreen)
        "sunset" -> Quad(AppleSmokeOrange, AppleSmokePink, AppleSmokePurple, AppleSmokeOrange)
        "aurora" -> Quad(AppleSmokeTeal, AppleSmokeBlue, AppleSmokeGreen, Color(0xFF00E676))
        else -> Quad(AppleSmokeBlue, AppleSmokeGreen, AppleSmokeOrange, AppleSmokeTeal) // "blue_green_orange"
    }

    val baseBackgroundColor = if (isLightMode) Color(0xFFFFFFFF) else Color(0xFF000000)
    val opacityFactor = if (isLightMode) alphaMultiplier * 0.55f else alphaMultiplier

    Box(
        modifier = modifier
            .background(baseBackgroundColor)
            .drawBehind {
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@drawBehind

                // Cloud 1: Blue drifting top-left
                val x1 = w * (0.28f + 0.16f * cos(phase1))
                val y1 = h * (0.18f + 0.14f * sin(phase1))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color1.copy(alpha = 0.42f * opacityFactor),
                            color1.copy(alpha = 0.15f * opacityFactor),
                            Color.Transparent
                        ),
                        center = Offset(x1, y1),
                        radius = w * 0.72f
                    )
                )

                // Cloud 2: Green drifting center-right
                val x2 = w * (0.76f + 0.14f * sin(phase2))
                val y2 = h * (0.35f + 0.16f * cos(phase2))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color2.copy(alpha = 0.36f * opacityFactor),
                            color2.copy(alpha = 0.10f * opacityFactor),
                            Color.Transparent
                        ),
                        center = Offset(x2, y2),
                        radius = w * 0.68f
                    )
                )

                // Cloud 3: Warm Orange drifting bottom-center
                val x3 = w * (0.40f + 0.20f * cos(phase2 * 0.8f))
                val y3 = h * (0.72f + 0.12f * sin(phase2 * 0.8f))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color3.copy(alpha = 0.34f * opacityFactor),
                            color3.copy(alpha = 0.08f * opacityFactor),
                            Color.Transparent
                        ),
                        center = Offset(x3, y3),
                        radius = w * 0.65f
                    )
                )

                // Cloud 4: Secondary accent drifting bottom-right
                val x4 = w * (0.82f + 0.12f * sin(phase1 * 0.75f))
                val y4 = h * (0.86f + 0.10f * cos(phase1 * 0.75f))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            color4.copy(alpha = 0.32f * opacityFactor),
                            color4.copy(alpha = 0.07f * opacityFactor),
                            Color.Transparent
                        ),
                        center = Offset(x4, y4),
                        radius = w * 0.62f
                    )
                )

                // Snow / Fog gentle ethereal mist layer
                val fogCenter = Offset(w * 0.5f + (snowDrift - 0.5f) * w * 0.25f, h * 0.5f)
                val fogTint = if (isLightMode) Color(0xFFF0F4F8) else Color.White
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            fogTint.copy(alpha = (if (isLightMode) 0.18f else 0.06f) * opacityFactor),
                            Color.Transparent
                        ),
                        center = fogCenter,
                        radius = w * 0.90f
                    )
                )
            },
        content = content
    )
}

private data class Quad<T>(val first: T, val second: T, val third: T, val fourth: T)
