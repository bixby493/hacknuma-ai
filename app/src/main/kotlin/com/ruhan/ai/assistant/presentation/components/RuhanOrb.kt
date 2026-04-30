package com.ruhan.ai.assistant.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

enum class OrbState {
    IDLE, LISTENING, THINKING, SPEAKING
}

@Composable
fun RuhanOrb(
    state: OrbState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.IDLE -> 3000
                    OrbState.LISTENING -> 800
                    OrbState.THINKING -> 1200
                    OrbState.SPEAKING -> 600
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 2000
                    else -> 8000
                },
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val primaryColor = when (state) {
        OrbState.IDLE -> Color(0xFF0088FF)
        OrbState.LISTENING -> Color(0xFF00E5FF)
        OrbState.THINKING -> Color(0xFFFFD700)
        OrbState.SPEAKING -> Color(0xFF00FF88)
    }

    val secondaryColor = when (state) {
        OrbState.IDLE -> Color(0xFF0044AA)
        OrbState.LISTENING -> Color(0xFF0099CC)
        OrbState.THINKING -> Color(0xFFFFA500)
        OrbState.SPEAKING -> Color(0xFF00AA55)
    }

    val ringExpand by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )

    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "particles"
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 3

        if (state == OrbState.LISTENING) {
            for (i in 0..2) {
                val scale = ringExpand + i * 0.3f
                val alpha = (1f - (scale - 0.5f) / 1.5f).coerceIn(0f, 0.5f)
                drawCircle(
                    color = primaryColor.copy(alpha = alpha),
                    radius = baseRadius * scale,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
        }

        if (state == OrbState.THINKING) {
            for (i in 0..7) {
                val angle = Math.toRadians((particleAngle + i * 45.0).toDouble())
                val dist = baseRadius * 1.3f
                val px = center.x + kotlin.math.cos(angle).toFloat() * dist
                val py = center.y + kotlin.math.sin(angle).toFloat() * dist
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = glowAlpha),
                    radius = 4f,
                    center = Offset(px, py)
                )
            }
        }

        drawOrb(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            pulseScale = pulseScale,
            rotation = rotation,
            glowAlpha = glowAlpha
        )

        if (state == OrbState.SPEAKING) {
            for (i in 0..4) {
                val waveRadius = baseRadius * (1.1f + i * 0.15f) * pulseScale
                val waveAlpha = (0.4f - i * 0.08f).coerceAtLeast(0.05f)
                drawCircle(
                    color = Color(0xFF00FF88).copy(alpha = waveAlpha),
                    radius = waveRadius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                )
            }
        }
    }
}

private fun DrawScope.drawOrb(
    primaryColor: Color,
    secondaryColor: Color,
    pulseScale: Float,
    rotation: Float,
    glowAlpha: Float
) {
    val center = Offset(size.width / 2, size.height / 2)
    val baseRadius = size.minDimension / 3

    // Outer glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = glowAlpha * 0.4f),
                primaryColor.copy(alpha = glowAlpha * 0.1f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * pulseScale * 1.8f
        ),
        radius = baseRadius * pulseScale * 1.8f,
        center = center
    )

    // Middle glow ring
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = glowAlpha * 0.6f),
                secondaryColor.copy(alpha = glowAlpha * 0.2f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * pulseScale * 1.3f
        ),
        radius = baseRadius * pulseScale * 1.3f,
        center = center
    )

    // Core orb
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor,
                secondaryColor,
                primaryColor.copy(alpha = 0.6f)
            ),
            center = Offset(
                center.x + kotlin.math.cos(Math.toRadians(rotation.toDouble())).toFloat() * 10f,
                center.y + kotlin.math.sin(Math.toRadians(rotation.toDouble())).toFloat() * 10f
            ),
            radius = baseRadius * pulseScale
        ),
        radius = baseRadius * pulseScale * 0.9f,
        center = center
    )

    // Inner bright core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                primaryColor.copy(alpha = 0.4f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 0.4f
        ),
        radius = baseRadius * 0.4f,
        center = center
    )
}
