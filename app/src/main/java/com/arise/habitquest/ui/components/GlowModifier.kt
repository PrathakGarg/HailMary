package com.arise.habitquest.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds a coloured shadow/glow effect around the composable.
 * Uses Canvas layer with BlurMaskFilter for the glow.
 */
fun Modifier.glowEffect(
    color: Color,
    radius: Dp = 12.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = android.graphics.Color.TRANSPARENT
        frameworkPaint.setShadowLayer(
            radius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            color.copy(alpha = 0.7f).toArgb()
        )
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = 12.dp.toPx(),
            radiusY = 12.dp.toPx(),
            paint = paint
        )
    }
}

fun Modifier.subtleGlow(color: Color, radius: Dp = 8.dp): Modifier =
    glowEffect(color = color, radius = radius)
