package com.arise.habitquest.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

private data class Particle(
    val angle: Float,           // radians
    val speed: Float,           // px/progress unit
    val size: Float,            // radius px
    val color: Color,
    val startDelay: Float       // 0..1 offset
)

/**
 * Full-screen particle burst.  Call with [trigger]=true to fire.
 * Particles radiate from the [origin] offset (defaults to centre).
 */
@Composable
fun ParticleBurst(
    modifier: Modifier = Modifier,
    trigger: Boolean,
    primaryColor: Color = Color(0xFFF59E0B),
    secondaryColor: Color = Color(0xFF7C3AED),
    particleCount: Int = 120,
    onFinished: () -> Unit = {}
) {
    val particles = remember {
        List(particleCount) {
            val colors = listOf(primaryColor, secondaryColor, Color.White)
            Particle(
                angle = Random.nextFloat() * 2 * PI.toFloat(),
                speed = Random.nextFloat() * 600f + 200f,
                size = Random.nextFloat() * 6f + 3f,
                color = colors.random(),
                startDelay = Random.nextFloat() * 0.2f
            )
        }
    }

    val animatableProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(trigger) {
        if (trigger) {
            animatableProgress.snapTo(0f)
            scope.launch {
                animatableProgress.animateTo(
                    1f,
                    animationSpec = tween(
                        durationMillis = 1200,
                        easing = FastOutSlowInEasing
                    )
                )
                onFinished()
            }
        }
    }

    val progress = animatableProgress.value

    Canvas(modifier = modifier) {
        if (progress <= 0f) return@Canvas
        val cx = size.width / 2f
        val cy = size.height / 2f

        particles.forEach { p ->
            val adjustedProgress = ((progress - p.startDelay) / (1f - p.startDelay))
                .coerceIn(0f, 1f)
            if (adjustedProgress <= 0f) return@forEach

            val distance = p.speed * adjustedProgress
            val x = cx + cos(p.angle) * distance
            val y = cy + sin(p.angle) * distance

            // Fade alpha and shrink near end
            val alpha = (1f - adjustedProgress).pow(0.5f)
            val radius = p.size * (1f - adjustedProgress * 0.5f)

            drawCircle(
                color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Smaller sparkle effect used inline on mission cards.
 */
@Composable
fun SparkleEffect(
    modifier: Modifier = Modifier,
    trigger: Boolean,
    color: Color = Color(0xFFF59E0B)
) {
    ParticleBurst(
        modifier = modifier,
        trigger = trigger,
        primaryColor = color,
        secondaryColor = color.copy(alpha = 0.6f),
        particleCount = 40
    )
}
