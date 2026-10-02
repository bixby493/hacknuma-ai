package com.ruhan.ai.assistant.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhan.ai.assistant.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

val OrbitronFont = FontFamily(
    Font(R.font.orbitron_regular, FontWeight.Normal),
    Font(R.font.orbitron_medium, FontWeight.Medium),
    Font(R.font.orbitron_bold, FontWeight.Bold)
)

val RajdhaniFont = FontFamily(
    Font(R.font.rajdhani_light, FontWeight.Light),
    Font(R.font.rajdhani_regular, FontWeight.Normal),
    Font(R.font.rajdhani_medium, FontWeight.Medium),
    Font(R.font.rajdhani_bold, FontWeight.Bold)
)

val SpaceGroteskFont = FontFamily(
    Font(R.font.space_grotesk_regular, FontWeight.Normal),
    Font(R.font.space_grotesk_bold, FontWeight.Bold)
)

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val hackerGreen = Color(0xFF00FF41)
    val darkBg = Color(0xFF0A0A0A)

    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showVersion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400)
        showTitle = true
        delay(600)
        showSubtitle = true
        delay(400)
        showVersion = true
        delay(1500)
        onSplashComplete()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val ring1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "ring1"
    )

    val ring2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "ring2"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreScale by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "coreScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated orb
            Canvas(modifier = Modifier.size(180.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val baseRadius = size.minDimension / 3

                // Outer glow
                val outerR = (baseRadius * 2.5f * coreScale).coerceAtLeast(0.01f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            hackerGreen.copy(alpha = pulseAlpha * 0.3f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = outerR
                    ),
                    radius = outerR,
                    center = center
                )

                // Ring 1
                rotate(ring1, pivot = center) {
                    drawArc(
                        color = hackerGreen.copy(alpha = 0.5f),
                        startAngle = 0f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = Offset(center.x - baseRadius * 1.4f, center.y - baseRadius * 0.45f),
                        size = androidx.compose.ui.geometry.Size(baseRadius * 2.8f, baseRadius * 0.9f),
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )
                }

                // Ring 2
                rotate(ring2, pivot = center) {
                    drawArc(
                        color = hackerGreen.copy(alpha = 0.35f),
                        startAngle = 60f,
                        sweepAngle = 200f,
                        useCenter = false,
                        topLeft = Offset(center.x - baseRadius * 0.5f, center.y - baseRadius * 1.35f),
                        size = androidx.compose.ui.geometry.Size(baseRadius * 1f, baseRadius * 2.7f),
                        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
                    )
                }

                // Core orb
                val coreR = (baseRadius * 0.8f * coreScale).coerceAtLeast(0.01f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            hackerGreen,
                            hackerGreen.copy(alpha = 0.5f),
                            hackerGreen.copy(alpha = 0.2f)
                        ),
                        center = center,
                        radius = coreR
                    ),
                    radius = coreR,
                    center = center
                )

                // White core
                val innerR = (baseRadius * 0.3f * coreScale).coerceAtLeast(0.01f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            hackerGreen.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = innerR
                    ),
                    radius = innerR,
                    center = center
                )

                // Orbiting particles
                for (i in 0..5) {
                    val angle = Math.toRadians((ring1 + i * 60.0))
                    val dist = baseRadius * 1.2f * coreScale
                    val px = center.x + cos(angle).toFloat() * dist
                    val py = center.y + sin(angle).toFloat() * dist
                    drawCircle(
                        color = hackerGreen.copy(alpha = 0.6f),
                        radius = 3f,
                        center = Offset(px, py)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(tween(600)) + scaleIn(tween(600))
            ) {
                Text(
                    text = "RUHAN AI",
                    color = hackerGreen,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = OrbitronFont,
                    letterSpacing = 6.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(tween(500))
            ) {
                Text(
                    text = "PERSONAL AI OPERATING SYSTEM",
                    color = hackerGreen.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = RajdhaniFont,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Version
            AnimatedVisibility(
                visible = showVersion,
                enter = fadeIn(tween(400))
            ) {
                Text(
                    text = "v2.0  ·  NEURAL CORE ONLINE",
                    color = hackerGreen.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    fontFamily = SpaceGroteskFont,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
