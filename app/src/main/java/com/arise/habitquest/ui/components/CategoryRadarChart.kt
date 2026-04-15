package com.arise.habitquest.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.ui.theme.AriseTypography
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp
import com.arise.habitquest.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

// ── Shared data class ─────────────────────────────────────────────────────────

/**
 * Per-category mission stats for the last 30 days.
 * Drives both the radar chart on StatusWindow and the pain-point analysis in History.
 */
data class CategoryStats(
    val category: MissionCategory,
    val total: Int,
    val completed: Int,   // includes mini completions
    val failed: Int,
    val skipped: Int,
    val miniAccepted: Int // completed but used the mini version
) {
    // Full completion = 1.0 weight, mini = 0.5, fail/skip = 0
    val masteryScore: Float get() {
        if (total == 0) return 0f
        val fullCompletions = (completed - miniAccepted).coerceAtLeast(0)
        return ((fullCompletions * 1.0f + miniAccepted * 0.5f) / total).coerceIn(0f, 1f)
    }
    val miniRate: Float get() = if (total == 0) 0f else miniAccepted.toFloat() / total
    val isPainPoint: Boolean get() = total >= 3 && masteryScore < 0.5f
    val isMiniCrutch: Boolean get() = total >= 3 && miniRate > 0.35f && !isPainPoint
}

// ── Colours & labels (same order as MissionCategory.entries) ─────────────────

val radarCategoryLabels = listOf("BODY", "MIND", "WORK", "SOCIAL", "WELL", "CREATE")
val radarCategoryColors = listOf(
    CrimsonCore,      // PHYSICAL
    BlueCore,         // MENTAL
    EmeraldCore,      // PRODUCTIVITY
    StatSenseColor,   // SOCIAL
    GoldCore,         // WELLNESS
    RankColorSS       // CREATIVITY
)

// ── Composable ────────────────────────────────────────────────────────────────

@Composable
fun CategoryRadarChart(
    stats: List<CategoryStats>,
    modifier: Modifier = Modifier
) {
    val ordered = MissionCategory.entries.map { cat -> stats.find { it.category == cat } }
    val scores  = ordered.map { it?.masteryScore ?: 0f }

    val gridColor   = ChartGrid
    val fillColor   = PurpleCore.copy(alpha = 0.27f)
    val strokeColor = PurpleCore
    val labelColor  = TextSecondary

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(220.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = size.minDimension / 2f * 0.62f
                val n = 6

                // Grid rings
                for (ring in 1..4) {
                    val r = radius * (ring / 4f)
                    val ringPath = Path()
                    for (i in 0 until n) {
                        val angle = Math.toRadians(-90.0 + i * 60.0)
                        val x = cx + r * cos(angle).toFloat()
                        val y = cy + r * sin(angle).toFloat()
                        if (i == 0) ringPath.moveTo(x, y) else ringPath.lineTo(x, y)
                    }
                    ringPath.close()
                    drawPath(ringPath, color = gridColor, style = Stroke(width = 1f))
                }

                // Axis spokes
                for (i in 0 until n) {
                    val angle = Math.toRadians(-90.0 + i * 60.0)
                    drawLine(
                        color = gridColor,
                        start = Offset(cx, cy),
                        end   = Offset(
                            cx + radius * cos(angle).toFloat(),
                            cy + radius * sin(angle).toFloat()
                        ),
                        strokeWidth = 1f
                    )
                }

                // Filled data polygon
                val dataPath = Path()
                scores.forEachIndexed { i, score ->
                    val angle = Math.toRadians(-90.0 + i * 60.0)
                    val r = radius * score.coerceAtLeast(0.05f)
                    val x = cx + r * cos(angle).toFloat()
                    val y = cy + r * sin(angle).toFloat()
                    if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                dataPath.close()
                drawPath(dataPath, color = fillColor)
                drawPath(dataPath, color = strokeColor, style = Stroke(width = 2.5f))

                // Category-coloured dots at each data point
                scores.forEachIndexed { i, score ->
                    val angle = Math.toRadians(-90.0 + i * 60.0)
                    val r = radius * score.coerceAtLeast(0.05f)
                    drawCircle(
                        color = radarCategoryColors[i],
                        radius = 5f,
                        center = Offset(
                            cx + r * cos(angle).toFloat(),
                            cy + r * sin(angle).toFloat()
                        )
                    )
                }

                // Axis labels via native canvas
                val paint = android.graphics.Paint().apply {
                    color = labelColor.hashCode()
                    // Use argb directly to avoid colour conversion issues
                    this.color = android.graphics.Color.argb(
                        (labelColor.alpha * 255).toInt(),
                        (labelColor.red   * 255).toInt(),
                        (labelColor.green * 255).toInt(),
                        (labelColor.blue  * 255).toInt()
                    )
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                val labelRadius = radius + 30f
                for (i in 0 until n) {
                    val angle = Math.toRadians(-90.0 + i * 60.0)
                    val lx = cx + labelRadius * cos(angle).toFloat()
                    val ly = cy + labelRadius * sin(angle).toFloat() + paint.textSize / 3f
                    drawContext.canvas.nativeCanvas.drawText(radarCategoryLabels[i], lx, ly, paint)
                }
            }
        }

        // Per-category score row
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MissionCategory.entries.forEachIndexed { i, cat ->
                val stat  = stats.find { it.category == cat }
                val score = stat?.masteryScore ?: 0f
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(score * 100).toInt()}%",
                        style = AriseTypography.labelSmall.copy(
                            color = radarCategoryColors[i],
                            fontSize = 10.sp
                        )
                    )
                    Text(
                        text = radarCategoryLabels[i],
                        style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 8.sp)
                    )
                }
            }
        }
    }
}
