package com.ruhan.ai.assistant.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.unit.dp

@Composable
fun FloatingRuhanButton(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_orb")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_pulse"
    )

    Canvas(modifier = modifier.size(56.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.5f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00FF41).copy(alpha = 0.3f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * pulse * 1.5f
            ),
            radius = radius * pulse * 1.5f,
            center = center
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00FF41),
                    Color(0xFF00CC33),
                    Color(0xFF004411)
                ),
                center = center,
                radius = radius * pulse
            ),
            radius = radius * pulse,
            center = center
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = radius * 0.3f,
            center = center
        )
    }
}
