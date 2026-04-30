package com.ruhan.ai.assistant.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF00E5FF),
    barCount: Int = 30
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        val barWidth = size.width / (barCount * 2f)
        val maxBarHeight = size.height * 0.8f

        for (i in 0 until barCount) {
            val x = (i * 2 + 0.5f) * barWidth
            val normalizedPosition = i.toFloat() / barCount

            val barHeight = if (isActive) {
                val wave1 = sin(normalizedPosition * 4f + phase).toFloat()
                val wave2 = sin(normalizedPosition * 7f - phase * 1.3f).toFloat()
                val combined = (wave1 + wave2) / 2f
                (0.15f + (combined + 1f) / 2f * 0.85f) * maxBarHeight
            } else {
                maxBarHeight * 0.08f
            }

            val alpha = if (isActive) 0.5f + (barHeight / maxBarHeight) * 0.5f else 0.3f

            drawRoundRect(
                color = color.copy(alpha = alpha),
                topLeft = Offset(x, (size.height - barHeight) / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f)
            )
        }
    }
}
