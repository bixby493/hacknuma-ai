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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.ruhan.ai.assistant.presentation.theme.RuhanThemeColors
import kotlin.math.cos
import kotlin.math.sin

enum class OrbState {
    IDLE, LISTENING, THINKING, SPEAKING
}

@Composable
fun RuhanOrb(
    state: OrbState,
    modifier: Modifier = Modifier
) {
    val colors = RuhanThemeColors.current
    val accent = colors.accent
    val accentDark = colors.textSecondary

    val infiniteTransition = rememberInfiniteTransition(label = "orb")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.IDLE -> 3000
                    OrbState.LISTENING -> 700
                    OrbState.THINKING -> 1000
                    OrbState.SPEAKING -> 500
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val ring1Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 1500
                    OrbState.SPEAKING -> 2000
                    else -> 6000
                },
                easing = LinearEasing
            )
        ),
        label = "ring1"
    )

    val ring2Rotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 2000
                    OrbState.SPEAKING -> 2500
                    else -> 8000
                },
                easing = LinearEasing
            )
        ),
        label = "ring2"
    )

    val ring3Rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing)
        ),
        label = "ring3"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.SPEAKING -> 400
                    OrbState.LISTENING -> 600
                    else -> 1500
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val ringExpand by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )

    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing)
        ),
        label = "particles"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831853f, // 2*PI
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "wave"
    )

    Canvas(modifier = modifier.size(220.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 3.2f

        // === LISTENING: Expanding sonar rings ===
        if (state == OrbState.LISTENING) {
            for (i in 0..3) {
                val scale = ringExpand + i * 0.25f
                val alpha = (1f - (scale - 0.5f) / 1.8f).coerceIn(0f, 0.4f)
                drawCircle(
                    color = accent.copy(alpha = alpha),
                    radius = baseRadius * scale,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }
        }

        // === THINKING: Orbiting particle constellation ===
        if (state == OrbState.THINKING) {
            for (i in 0..11) {
                val angle = Math.toRadians((particleAngle + i * 30.0))
                val dist = baseRadius * (1.2f + 0.15f * sin(angle * 2).toFloat())
                val px = center.x + cos(angle).toFloat() * dist
                val py = center.y + sin(angle).toFloat() * dist
                val pAlpha = (0.3f + 0.5f * ((i % 3) / 2f)) * glowAlpha
                drawCircle(
                    color = accent.copy(alpha = pAlpha),
                    radius = 3f + (i % 3) * 1.5f,
                    center = Offset(px, py)
                )
            }
        }

        // === Orbital ring 1 (tilted ellipse) ===
        rotate(ring1Rotation, pivot = center) {
            drawArc(
                color = accent.copy(alpha = glowAlpha * 0.5f),
                startAngle = 0f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(center.x - baseRadius * 1.35f, center.y - baseRadius * 0.4f),
                size = androidx.compose.ui.geometry.Size(baseRadius * 2.7f, baseRadius * 0.8f),
                style = Stroke(width = 1.8f, cap = StrokeCap.Round)
            )
        }

        // === Orbital ring 2 (perpendicular) ===
        rotate(ring2Rotation, pivot = center) {
            drawArc(
                color = accentDark.copy(alpha = glowAlpha * 0.4f),
                startAngle = 30f,
                sweepAngle = 200f,
                useCenter = false,
                topLeft = Offset(center.x - baseRadius * 0.45f, center.y - baseRadius * 1.3f),
                size = androidx.compose.ui.geometry.Size(baseRadius * 0.9f, baseRadius * 2.6f),
                style = Stroke(width = 1.5f, cap = StrokeCap.Round)
            )
        }

        // === Orbital ring 3 (diagonal) ===
        rotate(ring3Rotation + 45f, pivot = center) {
            drawArc(
                color = accent.copy(alpha = glowAlpha * 0.3f),
                startAngle = 60f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - baseRadius * 1.1f, center.y - baseRadius * 0.6f),
                size = androidx.compose.ui.geometry.Size(baseRadius * 2.2f, baseRadius * 1.2f),
                style = Stroke(width = 1.2f, cap = StrokeCap.Round)
            )
        }

        // === Outer glow (large diffuse) ===
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = glowAlpha * 0.35f),
                    accent.copy(alpha = glowAlpha * 0.08f),
                    Color.Transparent
                ),
                center = center,
                radius = baseRadius * pulseScale * 2f
            ),
            radius = baseRadius * pulseScale * 2f,
            center = center
        )

        // === Middle glow ring ===
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent.copy(alpha = glowAlpha * 0.55f),
                    accentDark.copy(alpha = glowAlpha * 0.15f),
                    Color.Transparent
                ),
                center = center,
                radius = baseRadius * pulseScale * 1.4f
            ),
            radius = baseRadius * pulseScale * 1.4f,
            center = center
        )

        // === Core orb with rotating gradient ===
        val gradientShift = Offset(
            center.x + cos(Math.toRadians(ring1Rotation.toDouble())).toFloat() * baseRadius * 0.15f,
            center.y + sin(Math.toRadians(ring1Rotation.toDouble())).toFloat() * baseRadius * 0.15f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    accent,
                    accentDark,
                    accent.copy(alpha = 0.5f)
                ),
                center = gradientShift,
                radius = baseRadius * pulseScale
            ),
            radius = baseRadius * pulseScale * 0.85f,
            center = center
        )

        // === Inner bright core (white hot center) ===
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.85f),
                    accent.copy(alpha = 0.5f),
                    Color.Transparent
                ),
                center = center,
                radius = baseRadius * 0.35f
            ),
            radius = baseRadius * 0.35f,
            center = center
        )

        // === SPEAKING: Animated sound wave bars ===
        if (state == OrbState.SPEAKING) {
            for (i in 0..7) {
                val angle = Math.toRadians((i * 45.0 + ring1Rotation))
                val waveHeight = baseRadius * 0.25f * (0.5f + 0.5f * sin(wavePhase + i * 0.8f))
                val innerDist = baseRadius * pulseScale * 0.95f
                val outerDist = innerDist + waveHeight
                val x1 = center.x + cos(angle).toFloat() * innerDist
                val y1 = center.y + sin(angle).toFloat() * innerDist
                val x2 = center.x + cos(angle).toFloat() * outerDist
                val y2 = center.y + sin(angle).toFloat() * outerDist
                drawLine(
                    color = accent.copy(alpha = 0.7f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
            // Outer wave rings
            for (i in 0..2) {
                val waveRadius = baseRadius * (1.15f + i * 0.12f) * pulseScale
                val waveAlpha = (0.35f - i * 0.1f).coerceAtLeast(0.05f)
                drawCircle(
                    color = accent.copy(alpha = waveAlpha),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(width = 1.2f)
                )
            }
        }

        // === Small orbiting nodes (always visible) ===
        for (i in 0..2) {
            val angle = Math.toRadians((ring1Rotation * (1 + i * 0.3f) + i * 120.0))
            val dist = baseRadius * 1.15f * pulseScale
            val nx = center.x + cos(angle).toFloat() * dist
            val ny = center.y + sin(angle).toFloat() * dist
            drawCircle(
                color = accent.copy(alpha = glowAlpha * 0.7f),
                radius = 3.5f,
                center = Offset(nx, ny)
            )
            // Tiny glow around node
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(nx, ny),
                    radius = 10f
                ),
                radius = 10f,
                center = Offset(nx, ny)
            )
        }
    }
}
