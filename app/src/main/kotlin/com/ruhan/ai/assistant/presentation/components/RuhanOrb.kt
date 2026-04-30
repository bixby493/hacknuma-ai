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
        OrbState.THINKING -> Color.White
        OrbState.SPEAKING -> Color(0xFF00FF88)
    }

    val secondaryColor = when (state) {
        OrbState.IDLE -> Color(0xFF0044AA)
        OrbState.LISTENING -> Color(0xFF0099CC)
        OrbState.THINKING -> Color(0xFFCCCCCC)
        OrbState.SPEAKING -> Color(0xFF00AA55)
    }

    Canvas(modifier = modifier.size(200.dp)) {
        drawOrb(
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            pulseScale = pulseScale,
            rotation = rotation,
            glowAlpha = glowAlpha
        )
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
