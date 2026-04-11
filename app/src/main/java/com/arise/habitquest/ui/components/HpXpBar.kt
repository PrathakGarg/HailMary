package com.arise.habitquest.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arise.habitquest.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun HpBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    showLabel: Boolean = true,
    damaged: Boolean = false   // triggers shake animation
) {
    val animatedFraction by animateFloatAsState(
        targetValue = if (max > 0) current.toFloat() / max else 0f,
        animationSpec = tween(600, easing = EaseOutQuart),
        label = "hp_fraction"
    )

    // Shake on damage
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(damaged) {
        if (damaged) {
            repeat(4) {
                shakeOffset.animateTo(8f, tween(50))
                shakeOffset.animateTo(-8f, tween(50))
            }
            shakeOffset.animateTo(0f, tween(50))
        }
    }

    Column(modifier = modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("HP", style = SystemTextStyle.copy(fontSize = 10.sp), color = CrimsonLight)
                Text("$current / $max", style = SystemTextStyle.copy(fontSize = 10.sp), color = TextDim)
            }
            Spacer(Modifier.height(2.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(50))
                .background(CrimsonDim.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(CrimsonDim, CrimsonCore, CrimsonLight)
                        )
                    )
            )
        }
    }
}

@Composable
fun XpBar(
    current: Long,
    max: Long,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    showLabel: Boolean = true
) {
    val animatedFraction by animateFloatAsState(
        targetValue = if (max > 0) current.toFloat() / max else 0f,
        animationSpec = tween(800, easing = EaseOutQuart),
        label = "xp_fraction"
    )

    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("XP", style = SystemTextStyle.copy(fontSize = 10.sp), color = PurpleLight)
                Text("$current / $max", style = SystemTextStyle.copy(fontSize = 10.sp), color = TextDim)
            }
            Spacer(Modifier.height(2.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(50))
                .background(PurpleDim.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PurpleCore, PurpleLight, GoldCore)
                        )
                    )
            )
        }
    }
}

private val EaseOutQuart: Easing = CubicBezierEasing(0.165f, 0.84f, 0.44f, 1.0f)
