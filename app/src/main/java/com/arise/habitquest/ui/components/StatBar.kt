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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arise.habitquest.domain.model.Stat
import com.arise.habitquest.ui.theme.*

@Composable
fun StatBar(
    stat: Stat,
    value: Int,
    maxValue: Int = 999,
    modifier: Modifier = Modifier,
    showGain: Int = 0   // >0 shows a +N floating indicator
) {
    val color = statColor(stat)
    val fraction by animateFloatAsState(
        targetValue = value.toFloat() / maxValue.coerceAtLeast(1),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "stat_${stat.name}"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Stat name
        Text(
            text = stat.name,
            style = AriseTypography.labelSmall.copy(
                color = color,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.width(44.dp)
        )

        // Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), color))
                    )
            )
        }

        // Value
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value.toString(),
                style = AriseTypography.labelSmall.copy(color = TextSecondary, fontSize = 12.sp),
                modifier = Modifier.width(28.dp)
            )
            if (showGain > 0) {
                Text(
                    text = "+$showGain",
                    style = AriseTypography.labelSmall.copy(
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

fun statColor(stat: Stat): Color = when (stat) {
    Stat.STR -> StatStrColor
    Stat.AGI -> StatAgiColor
    Stat.INT -> StatIntColor
    Stat.VIT -> StatVitColor
    Stat.END -> StatEndColor
    Stat.SENSE -> StatSenseColor
}
