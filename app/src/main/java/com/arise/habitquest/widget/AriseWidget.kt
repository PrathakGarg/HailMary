package com.arise.habitquest.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.arise.habitquest.MainActivity
import com.arise.habitquest.data.local.database.AppDatabase
import com.arise.habitquest.data.time.TimeProvider
import kotlinx.coroutines.flow.first

class AriseWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(140.dp, 72.dp),
            DpSize(250.dp, 110.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch data directly from Room (widget runs outside Hilt scope)
        val db = AppDatabase.getInstance(context)
        val profile = db.userProfileDao().getUserProfile()
        val todayMissions = try {
            val today = TimeProvider.getInstance(context).sessionDay().toString()
            db.missionDao().getMissionsForDate(today)
        } catch (e: Exception) { emptyList() }

        val hunterName = profile?.hunterName ?: "HUNTER"
        val rank = profile?.rank ?: "E"
        val hp = profile?.hp ?: 100
        val maxHp = profile?.maxHp ?: 100
        val xp = profile?.xp ?: 0L
        val xpToNext = profile?.xpToNextLevel ?: 100L
        val streak = profile?.streakCurrent ?: 0
        val completedToday = todayMissions.count { it.isCompleted }
        val totalToday = todayMissions.size
        val hpFraction = if (maxHp > 0) hp.toFloat() / maxHp else 0f
        val xpFraction = if (xpToNext > 0) xp.toFloat() / xpToNext else 0f

        provideContent {
            AriseWidgetContent(
                hunterName = hunterName,
                rank = rank,
                hp = hp, maxHp = maxHp, hpFraction = hpFraction,
                xpFraction = xpFraction,
                streak = streak,
                completedToday = completedToday,
                totalToday = totalToday
            )
        }
    }
}

@Composable
private fun AriseWidgetContent(
    hunterName: String,
    rank: String,
    hp: Int, maxHp: Int, hpFraction: Float,
    xpFraction: Float,
    streak: Int,
    completedToday: Int,
    totalToday: Int
) {
    val isLowHp = hpFraction <= 0.30f
    val bgColor = if (isLowHp) ColorProvider(Color(0xFF1A0D0D)) else ColorProvider(Color(0xFF0D0D1A))
    val purpleColor = ColorProvider(Color(0xFF7C3AED))
    val goldColor = ColorProvider(Color(0xFFF59E0B))
    val crimsonColor = if (isLowHp) ColorProvider(Color(0xFFFF6B6B)) else ColorProvider(Color(0xFFEF4444))
    val textPrimary = ColorProvider(Color(0xFFE2E8F0))
    val textDim = if (isLowHp) ColorProvider(Color(0xFF94A3B8)) else ColorProvider(Color(0xFF475569))
    val rankArgb = widgetRankColor(rank)
    val rankColor = ColorProvider(Color(rankArgb))
    val rankColorDim = ColorProvider(Color(rankArgb).copy(alpha = 0.18f))
    val rankColorDark = ColorProvider(Color(0xFF0D0D1A)) // dark badge interior
    val localSize = LocalSize.current
    val isCompact = localSize.width < 220.dp || localSize.height < 110.dp

    if (isCompact) {
        CompactWidgetContent(
            bgColor = bgColor,
            textPrimary = textPrimary,
            textDim = textDim,
            rankColor = rankColor,
            hunterName = hunterName,
            rank = rank,
            streak = streak,
            completedToday = completedToday,
            totalToday = totalToday,
            isLowHp = isLowHp
        )
        return
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Top row: Rank badge + Name + Streak ──────────────────────────
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank badge: halo → colored ring → dark interior → colored text
                // (simulates the app's RankBadge using nested rounded boxes)
                Box(
                    modifier = GlanceModifier
                        .size(42.dp)
                        .background(rankColorDim)
                        .cornerRadius(21.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .background(rankColor)
                            .cornerRadius(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(28.dp)
                                .background(rankColorDark)
                                .cornerRadius(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                rank,
                                style = TextStyle(
                                    color = rankColor,
                                    fontSize = if (rank.length > 2) 7.sp else 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
                Spacer(GlanceModifier.width(8.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        hunterName.uppercase(),
                        style = TextStyle(color = textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "RANK $rank HUNTER",
                        style = TextStyle(color = rankColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "🔥 $streak",
                        style = TextStyle(color = goldColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "day streak",
                        style = TextStyle(color = textDim, fontSize = 9.sp)
                    )
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            // ── HP / XP stats (Glance 1.0 doesn't support fillMaxWidth(fraction)) ──
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "HP",
                    style = TextStyle(
                        color = crimsonColor,
                        fontSize = if (isLowHp) 10.sp else 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    "$hp / $maxHp  (${(hpFraction * 100).toInt()}%)",
                    style = TextStyle(color = textDim, fontSize = 9.sp)
                )
                if (isLowHp) {
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        "LOW HP",
                        style = TextStyle(color = crimsonColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    "XP",
                    style = TextStyle(color = purpleColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    "${(xpFraction * 100).toInt()}%",
                    style = TextStyle(color = textDim, fontSize = 9.sp)
                )
            }

            Spacer(GlanceModifier.height(6.dp))

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                SimulatedProgressBar(
                    label = "HP",
                    fraction = hpFraction,
                    filledColor = crimsonColor,
                    emptyColor = ColorProvider(Color(0xFF1F2937)),
                    textDim = textDim
                )
            }

            Spacer(GlanceModifier.height(4.dp))

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                SimulatedProgressBar(
                    label = "XP",
                    fraction = xpFraction,
                    filledColor = purpleColor,
                    emptyColor = ColorProvider(Color(0xFF1F2937)),
                    textDim = textDim
                )
            }

            Spacer(GlanceModifier.height(8.dp))

            // ── Gates count ──────────────────────────────────────────────────
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TODAY'S GATES:",
                    style = TextStyle(color = textDim, fontSize = 10.sp)
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    "$completedToday / $totalToday",
                    style = TextStyle(
                        color = if (completedToday == totalToday && totalToday > 0) goldColor else textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    if (completedToday == totalToday && totalToday > 0) "ALL CLEAR ✓" else "TAP TO ENTER ▶",
                    style = TextStyle(
                        color = if (completedToday == totalToday && totalToday > 0) goldColor else purpleColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun SimulatedProgressBar(
    label: String,
    fraction: Float,
    filledColor: ColorProvider,
    emptyColor: ColorProvider,
    textDim: ColorProvider
) {
    val totalWidth = 120.dp
    val clamped = fraction.coerceIn(0f, 1f)
    val filledWidth = (totalWidth.value * clamped).dp
    val emptyWidth = (totalWidth.value - filledWidth.value).dp

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = TextStyle(color = textDim, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(GlanceModifier.width(6.dp))
        Row {
            Box(
                modifier = GlanceModifier
                    .height(6.dp)
                    .width(filledWidth)
                    .background(filledColor)
                    .cornerRadius(3.dp)
            ) {}
            Box(
                modifier = GlanceModifier
                    .height(6.dp)
                    .width(emptyWidth)
                    .background(emptyColor)
                    .cornerRadius(3.dp)
            ) {}
        }
    }
}

@Composable
private fun CompactWidgetContent(
    bgColor: ColorProvider,
    textPrimary: ColorProvider,
    textDim: ColorProvider,
    rankColor: ColorProvider,
    hunterName: String,
    rank: String,
    streak: Int,
    completedToday: Int,
    totalToday: Int,
    isLowHp: Boolean
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity<MainActivity>())
            .padding(10.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$rank • ${hunterName.uppercase()}",
                    style = TextStyle(color = rankColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    "🔥$streak",
                    style = TextStyle(color = ColorProvider(Color(0xFFF59E0B)), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
            }
            Spacer(GlanceModifier.height(6.dp))
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "GATES $completedToday/$totalToday",
                    style = TextStyle(color = textPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    if (isLowHp) "LOW HP" else "OPEN",
                    style = TextStyle(
                        color = if (isLowHp) ColorProvider(Color(0xFFFF6B6B)) else textDim,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

class AriseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AriseWidget()
}

suspend fun refreshAriseWidget(context: Context) {
    AriseWidget().updateAll(context)
}

/** Maps a rank name string to its ARGB long — mirrors rankColor() in RankBadge.kt. */
private fun widgetRankColor(rank: String): Long = when (rank) {
    "E"       -> 0xFF9CA3AFL  // Grey
    "D"       -> 0xFF10B981L  // Emerald
    "C"       -> 0xFF3B82F6L  // Blue
    "B"       -> 0xFF8B5CF6L  // Violet
    "A"       -> 0xFFF59E0BL  // Gold
    "S"       -> 0xFFEF4444L  // Crimson
    "SS"      -> 0xFFEC4899L  // Pink
    "SSS"     -> 0xFFF97316L  // Orange
    "MONARCH" -> 0xFFEAB308L  // Bright gold
    else      -> 0xFF9CA3AFL  // fallback grey
}
