package com.arise.habitquest.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arise.habitquest.domain.model.Rank
import com.arise.habitquest.ui.theme.*

@Composable
fun RankBadge(
    rank: Rank,
    size: Dp = 56.dp,
    pulsing: Boolean = true,
    modifier: Modifier = Modifier
) {
    val rankColor = rankColor(rank)
    val glowColor = rankColor.copy(alpha = 0.4f)

    val infiniteTransition = rememberInfiniteTransition(label = "rank_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (pulsing) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (pulsing) 0.7f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size * 1.3f)
            .scale(pulseScale)
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(size * 1.25f)
                .clip(CircleShape)
                .background(rankColor.copy(alpha = glowAlpha * 0.15f))
                .border(1.dp, rankColor.copy(alpha = glowAlpha), CircleShape)
        )
        // Main badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            rankColor.copy(alpha = 0.3f),
                            BackgroundCard,
                            BackgroundDeep
                        )
                    )
                )
                .border(2.dp, rankColor, CircleShape)
                .glowEffect(glowColor, radius = 10.dp)
        ) {
            Text(
                text = rank.displayName,
                style = TextStyle(
                    color = rankColor,
                    fontSize = when {
                        rank.displayName.length > 2 -> (size.value * 0.28f).sp
                        else -> (size.value * 0.38f).sp
                    },
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
        }
    }
}

fun rankColor(rank: Rank): Color = when (rank) {
    Rank.E -> RankColorE
    Rank.D -> RankColorD
    Rank.C -> RankColorC
    Rank.B -> RankColorB
    Rank.A -> RankColorA
    Rank.S -> RankColorS
    Rank.SS -> RankColorSS
    Rank.SSS -> RankColorSSS
    Rank.MONARCH -> RankColorMonarch
}
