package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkGlassBase
import com.example.ui.theme.DarkGlassRim
import com.example.ui.theme.GoogleSmokeBlue
import com.example.ui.theme.GoogleSmokeGreen
import com.example.ui.theme.GoogleSmokeRed
import com.example.ui.theme.GoogleSmokeYellow
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
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    transparencyAlpha: Float = 0.65f,
    isHighContrast: Boolean = false,
    contentAlignment: Alignment = Alignment.Center,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val effectiveAlpha = if (isHighContrast) 0.95f else transparencyAlpha.coerceIn(0.2f, 0.95f)

    val surfaceBrush = Brush.verticalGradient(
        colors = listOf(
            DarkGlassBase.copy(alpha = (effectiveAlpha * 1.15f).coerceAtMost(0.98f)),
            DarkGlassBase.copy(alpha = effectiveAlpha)
        )
    )

    val rimBrush = Brush.verticalGradient(
        colors = listOf(
            SpecularHighlight.copy(alpha = if (isHighContrast) 0.8f else 0.45f),
            DarkGlassRim.copy(alpha = if (isHighContrast) 0.6f else 0.25f),
            Color(0x0A000000)
        )
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = Color.White),
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(surfaceBrush)
            .border(width = 1.dp, brush = rimBrush, shape = shape)
            .then(clickModifier),
        contentAlignment = contentAlignment,
        content = content
    )
}

/**
 * Smooth ambient background colored gradient smoke moving like snow / fog gently colored
 * with Google colors (Blue, Red, Yellow, Green) drifting softly over deep black obsidian.
 */
@Composable
fun LiquidSmokeBackground(
    modifier: Modifier = Modifier,
    alphaMultiplier: Float = 0.35f,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "smoke_motion")

    // Slow organic drift cycles
    val phase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val snowDrift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "snowDrift"
    )

    Box(
        modifier = modifier
            .background(Color(0xFF000000))
            .drawBehind {
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@drawBehind

                // Google Blue cloud drifting top-left
                val blueX = w * (0.25f + 0.15f * cos(phase1))
                val blueY = h * (0.20f + 0.12f * sin(phase1))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoogleSmokeBlue.copy(alpha = 0.38f * alphaMultiplier),
                            GoogleSmokeBlue.copy(alpha = 0.12f * alphaMultiplier),
                            Color.Transparent
                        ),
                        center = Offset(blueX, blueY),
                        radius = w * 0.70f
                    )
                )

                // Google Red cloud drifting top-right
                val redX = w * (0.75f + 0.12f * sin(phase2))
                val redY = h * (0.25f + 0.15f * cos(phase2))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoogleSmokeRed.copy(alpha = 0.32f * alphaMultiplier),
                            GoogleSmokeRed.copy(alpha = 0.08f * alphaMultiplier),
                            Color.Transparent
                        ),
                        center = Offset(redX, redY),
                        radius = w * 0.65f
                    )
                )

                // Google Yellow / Amber cloud drifting center-bottom
                val yellowX = w * (0.45f + 0.18f * cos(phase2 * 0.8f))
                val yellowY = h * (0.70f + 0.10f * sin(phase2 * 0.8f))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoogleSmokeYellow.copy(alpha = 0.28f * alphaMultiplier),
                            GoogleSmokeYellow.copy(alpha = 0.06f * alphaMultiplier),
                            Color.Transparent
                        ),
                        center = Offset(yellowX, yellowY),
                        radius = w * 0.60f
                    )
                )

                // Google Green cloud drifting bottom-right / ambient snow-fog
                val greenX = w * (0.80f + 0.10f * sin(phase1 * 0.7f))
                val greenY = h * (0.85f + 0.08f * cos(phase1 * 0.7f))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GoogleSmokeGreen.copy(alpha = 0.30f * alphaMultiplier),
                            GoogleSmokeGreen.copy(alpha = 0.07f * alphaMultiplier),
                            Color.Transparent
                        ),
                        center = Offset(greenX, greenY),
                        radius = w * 0.62f
                    )
                )

                // Ethereal ambient soft snow/fog mist particles
                val fogCenter = Offset(w * 0.5f + (snowDrift - 0.5f) * w * 0.2f, h * 0.45f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f * alphaMultiplier),
                            Color.Transparent
                        ),
                        center = fogCenter,
                        radius = w * 0.85f
                    )
                )
            },
        content = content
    )
}
