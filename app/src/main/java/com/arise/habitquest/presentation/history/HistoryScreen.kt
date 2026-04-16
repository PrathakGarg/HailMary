package com.arise.habitquest.presentation.history

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.ui.components.AriseTopBar
import com.arise.habitquest.ui.components.CategoryRadarChart
import com.arise.habitquest.ui.components.CategoryStats
import com.arise.habitquest.ui.theme.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeep,
        topBar = { AriseTopBar(title = "BATTLE RECORD", onBack = onBack) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurpleCore)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("history_screen")
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Summary stats row ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_summary_row"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatCard("MISSIONS", "${state.totalMissionsCompleted}", Modifier.weight(1f).testTag("history_summary_missions"))
                SummaryStatCard("TOTAL XP", "${state.totalXpEarned}", Modifier.weight(1f).testTag("history_summary_xp"))
                SummaryStatCard("BEST STREAK", "${state.bestStreak}d", Modifier.weight(1f).testTag("history_summary_streak"))
            }

            // ── Calendar heatmap ─────────────────────────────────────────────
            SectionHeader("GATE HISTORY — LAST 90 DAYS")
            CalendarHeatmap(days = state.calendarDays, today = state.today, currentStreak = state.currentStreak, joinDate = state.joinDate)

            // ── Legend ───────────────────────────────────────────────────────
            HeatmapLegend()

            // ── XP bar chart (weekly) ────────────────────────────────────────
            if (state.weeklyXp.any { it.second > 0 }) {
                SectionHeader("WEEKLY XP EARNED")
                WeeklyXpChart(weeks = state.weeklyXp, modifier = Modifier.testTag("history_weekly_xp_chart"))
            }

            // ── Analysis insights ─────────────────────────────────────────────
            if (state.insights.isNotEmpty()) {
                SectionHeader("SYSTEM ANALYSIS")
                state.insights.forEachIndexed { index, insight ->
                    InsightCard(insight = insight, modifier = Modifier.testTag("history_insight_$index"))
                }
            }

            // ── Category radar + pain points ──────────────────────────────────
            if (state.categoryStats.any { it.total > 0 }) {
                SectionHeader("HUNTER BALANCE — LAST 30 DAYS")
                CategoryRadarChart(stats = state.categoryStats)
                val painPoints = state.categoryStats.filter { it.isPainPoint }
                val crutches   = state.categoryStats.filter { it.isMiniCrutch }
                if (painPoints.isNotEmpty() || crutches.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    SectionHeader("WEAKNESS REPORT")
                    painPoints.take(2).forEach { PainPointCard(it, isCrutch = false) }
                    crutches.take(1).forEach   { PainPointCard(it, isCrutch = true)  }
                }
            }

            // ── Empty state ───────────────────────────────────────────────────
            if (state.calendarDays.none { it.hasData }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_empty_state")
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "[ No battle records found.\nComplete your first gate to begin your history. ]",
                        style = SystemTextStyle.copy(color = TextDim, fontSize = 13.sp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SummaryStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(BackgroundCard, RoundedCornerShape(10.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = AriseTypography.titleLarge.copy(color = GoldCore))
            Text(label, style = AriseTypography.labelSmall.copy(color = TextDim, letterSpacing = 1.sp, fontSize = 9.sp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = AriseTypography.labelMedium.copy(color = PurpleLight, letterSpacing = 2.sp)
    )
}

@Composable
private fun CalendarHeatmap(days: List<DayEntry>, today: LocalDate, currentStreak: Int, joinDate: LocalDate) {
    val weeks = days.chunked(7)
    val loggedDays = days.count { it.hasData }
    val perfectDays = days.count { it.hasData && it.completionRate >= 1.0f }
    val trailing7 = days.takeLast(7)
    val lastWeekRate = if (trailing7.isNotEmpty()) {
        (trailing7.count { it.hasData && it.completionRate > 0f } * 100) / trailing7.size
    } else 0

    // Compute streak cell dates (consecutive days ending at today with completion > 0)
    val streakDates = remember(days, currentStreak) {
        if (currentStreak <= 0) emptySet()
        else days.asReversed()
            .take(currentStreak + 1) // +1 to include today even if no data yet
            .filter { it.hasData && it.completionRate > 0f }
            .map { it.date }
            .toSet()
    }

    // Tap-to-inspect state
    var selectedEntry by remember { mutableStateOf<DayEntry?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .testTag("history_heatmap")
                .drawBehind {
                    // Thin vertical separator between Friday and Saturday columns
                    val labelWidthPx = 28.dp.toPx()
                    val gapPx = 4.dp.toPx()
                    val gridStart = labelWidthPx + gapPx
                    val gridWidth = size.width - gridStart
                    val cellWidth = (gridWidth - 6 * gapPx) / 7f
                    val lineX = gridStart + 5 * (cellWidth + gapPx) - gapPx / 2f

                    drawLine(
                        color = BorderAccent,
                        start = Offset(lineX, 0f),
                        end = Offset(lineX, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Day-of-week header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Spacer(Modifier.width(28.dp))
                    listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { i, day ->
                        Text(
                            day,
                            modifier = Modifier.weight(1f),
                            style = AriseTypography.labelSmall.copy(
                                color = if (i >= 5) TextSecondary else TextDim,
                                fontSize = 9.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                weeks.forEachIndexed { weekIndex, week ->
                    val firstOfWeek = week.firstOrNull()?.date
                    // Check if this row contains a 1st-of-month cell
                    val monthStartEntry = week.firstOrNull { it.date.dayOfMonth == 1 }
                    val showMonthLabel = weekIndex > 0 && monthStartEntry != null

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Month label on left — show at month boundaries or first week
                        val weekLabel = when {
                            showMonthLabel ->
                                monthStartEntry!!.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3)
                            weekIndex == 0 && firstOfWeek != null ->
                                firstOfWeek.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3)
                            else -> ""
                        }
                        Text(
                            weekLabel,
                            modifier = Modifier.width(28.dp),
                            style = AriseTypography.labelSmall.copy(
                                color = if (showMonthLabel) PurpleLight else TextDim,
                                fontSize = 8.sp,
                                fontWeight = if (showMonthLabel) FontWeight.Bold else FontWeight.Normal
                            )
                        )

                        val paddedWeek = week + List(7 - week.size) { null }
                        paddedWeek.forEachIndexed { dayIndex, entry ->
                            val isToday = entry?.date == today
                            val isInStreak = entry?.date in streakDates
                            val isRestDay = entry?.wasRestDay == true

                            val isMissed = entry != null && !entry.hasData && !isRestDay
                                && entry.date.isBefore(today)
                                && !entry.date.isBefore(joinDate)

                            val cellColor = when {
                                entry == null -> Color.Transparent
                                isToday && !entry.hasData -> PurpleCore.copy(alpha = 0.35f)
                                isToday && entry.hasData -> heatmapColor(entry.completionRate)
                                isRestDay -> RestDayColor
                                isMissed -> MissedDayColor
                                !entry.hasData -> BackgroundElevated  // future
                                else -> heatmapColor(entry.completionRate)
                            }

                            val borderWidth = when {
                                isToday -> 1.5.dp
                                isInStreak -> 1.dp
                                else -> 0.5.dp
                            }
                            val borderColor = when {
                                isToday -> GoldCore
                                isInStreak -> PurpleLight.copy(alpha = 0.6f)
                                else -> BorderDefault.copy(alpha = 0.3f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor)
                                    .border(borderWidth, borderColor, RoundedCornerShape(3.dp))
                                    .then(
                                        if (entry != null) Modifier.clickable {
                                            selectedEntry = if (selectedEntry?.date == entry.date) null else entry
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Rest day dot indicator
                                if (isRestDay) {
                                    Box(
                                        Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(TextDim.copy(alpha = 0.6f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Tap-to-inspect detail card ───────────────────────────────
        selectedEntry?.let { entry ->
            InspectCard(entry = entry, isToday = entry.date == today, joinDate = joinDate)
        }

        Spacer(Modifier.height(4.dp))
        // ── Summary stats row below heatmap ──────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "$loggedDays/91 days logged",
                style = AriseTypography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
            )
            Text("•", style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 10.sp))
            Text(
                "$perfectDays perfect",
                style = AriseTypography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
            )
            Text("•", style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 10.sp))
            Text(
                "7d active $lastWeekRate%",
                style = AriseTypography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
            )
        }
    }
}

// ── 6-tier color ramp ─────────────────────────────────────────────────────────
private fun heatmapColor(rate: Float): Color = when {
    rate >= 1.0f  -> EmeraldCore                     // 100% — pure emerald
    rate >= 0.90f -> EmeraldCore.copy(alpha = 0.85f) // 90-99% — near-perfect
    rate >= 0.75f -> EmeraldCore.copy(alpha = 0.65f) // 75-89%
    rate >= 0.50f -> GoldCore.copy(alpha = 0.60f)    // 50-74%
    rate >= 0.25f -> GoldCore.copy(alpha = 0.35f)    // 25-49% — dim gold
    rate > 0f     -> CrimsonCore.copy(alpha = 0.45f) // 1-24% — crimson
    else          -> BackgroundElevated
}

// Subtle colors for special cell states
private val RestDayColor = Color(0xFF1A1A3E)   // muted blue-purple tint
private val MissedDayColor = Color(0xFF2A0F0F) // faint crimson tint

// ── Tap-to-inspect card ───────────────────────────────────────────────────────
@Composable
private fun InspectCard(entry: DayEntry, isToday: Boolean, joinDate: LocalDate) {
    val dayName = entry.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val monthDay = "${entry.date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${entry.date.dayOfMonth}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(BackgroundCard, RoundedCornerShape(10.dp))
            .border(0.5.dp, BorderAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val isMissed = !entry.hasData && !entry.wasRestDay && entry.date.isBefore(LocalDate.now())
            && !entry.date.isBefore(joinDate)
        // Color swatch
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    when {
                        entry.wasRestDay -> RestDayColor
                        isMissed -> MissedDayColor
                        entry.hasData -> heatmapColor(entry.completionRate)
                        else -> BackgroundElevated
                    }
                )
                .then(
                    if (isToday) Modifier.border(1.5.dp, GoldCore, RoundedCornerShape(4.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (entry.wasRestDay) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(TextDim.copy(alpha = 0.6f)))
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "$dayName, $monthDay${if (isToday) " (TODAY)" else ""}",
                style = AriseTypography.labelSmall.copy(
                    color = if (isToday) GoldCore else TextPrimary,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            )
            when {
                entry.wasRestDay -> Text(
                    "Rest Day — no gates assigned",
                    style = AriseTypography.bodySmall.copy(color = TextDim, fontSize = 10.sp)
                )
                !entry.hasData -> Text(
                    when {
                        entry.date.isAfter(LocalDate.now()) -> "Future"
                        isToday -> "No gates logged yet"
                        else -> "Missed — no gates logged"
                    },
                    style = AriseTypography.bodySmall.copy(
                        color = if (isMissed) CrimsonCore.copy(alpha = 0.7f) else TextDim,
                        fontSize = 10.sp
                    )
                )
                else -> {
                    Text(
                        "${(entry.completionRate * 100).toInt()}% complete • ${entry.totalMissions} missions",
                        style = AriseTypography.bodySmall.copy(color = TextSecondary, fontSize = 10.sp)
                    )
                    if (entry.xpGained > 0) {
                        Text(
                            "+${entry.xpGained} XP",
                            style = AriseTypography.labelSmall.copy(color = GoldCore, fontSize = 10.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // ── COMPLETION GRADIENT ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundCard, RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "COMPLETION",
                style = AriseTypography.labelSmall.copy(
                    color = PurpleLight, fontSize = 8.sp, letterSpacing = 1.5.sp
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("0%", style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 8.sp))
                listOf(
                    BackgroundElevated,
                    CrimsonCore.copy(0.45f),
                    GoldCore.copy(0.35f),
                    GoldCore.copy(0.6f),
                    EmeraldCore.copy(0.65f),
                    EmeraldCore.copy(0.85f),
                    EmeraldCore
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
                Text("100%", style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 8.sp))
            }
        }

        // ── SPECIAL MARKERS ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Today marker card
            LegendMarkerCard(
                modifier = Modifier.weight(1f),
                label = "TODAY",
                swatch = {
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(PurpleCore.copy(alpha = 0.35f))
                            .border(1.5.dp, GoldCore, RoundedCornerShape(3.dp))
                    )
                }
            )
            // Streak marker card
            LegendMarkerCard(
                modifier = Modifier.weight(1f),
                label = "STREAK",
                swatch = {
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(EmeraldCore.copy(alpha = 0.65f))
                            .border(1.dp, PurpleLight.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                    )
                }
            )
            // Rest day marker card
            LegendMarkerCard(
                modifier = Modifier.weight(1f),
                label = "REST",
                swatch = {
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(RestDayColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(TextDim.copy(alpha = 0.5f))
                        )
                    }
                }
            )
            // Missed marker card
            LegendMarkerCard(
                modifier = Modifier.weight(1f),
                label = "MISSED",
                swatch = {
                    Box(
                        Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MissedDayColor)
                    )
                }
            )
        }
    }
}

@Composable
private fun LegendMarkerCard(
    modifier: Modifier = Modifier,
    label: String,
    swatch: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .background(BackgroundCard, RoundedCornerShape(8.dp))
            .border(0.5.dp, BorderDefault.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        swatch()
        Text(
            label,
            style = AriseTypography.labelSmall.copy(
                color = TextDim, fontSize = 8.sp, letterSpacing = 1.sp
            )
        )
    }
}

@Composable
private fun WeeklyXpChart(weeks: List<Pair<String, Long>>, modifier: Modifier = Modifier) {
    val maxXp = weeks.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1L
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        weeks.forEach { (label, xp) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (xp > 0) {
                    Text(
                        "$xp",
                        style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 8.sp)
                    )
                }
                val fraction = (xp.toFloat() / maxXp).coerceIn(0.02f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(
                            if (label.startsWith("This")) PurpleCore
                            else PurpleDim.copy(alpha = 0.6f)
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 7.sp),
                    textAlign = TextAlign.Center,
                    lineHeight = 9.sp
                )
            }
        }
    }
}

@Composable
private fun InsightCard(insight: AnalysisInsight, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundCard, RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            if (insight.isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
            contentDescription = null,
            tint = if (insight.isPositive) EmeraldCore else CrimsonCore,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(insight.label, style = AriseTypography.labelSmall.copy(color = TextDim, letterSpacing = 1.sp, fontSize = 10.sp))
            Text(insight.value, style = AriseTypography.titleSmall.copy(color = TextPrimary))
            Text(insight.subtext, style = AriseTypography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
        }
    }
}

// ── Pain point / mini-crutch card ─────────────────────────────────────────────

@Composable
private fun PainPointCard(stats: CategoryStats, isCrutch: Boolean) {
    val borderColor = if (isCrutch) Color(0xFFF59E0B) else CrimsonCore
    val message = if (isCrutch) crutchMessage(stats.category) else painMessage(stats.category)
    val subtitle = if (isCrutch)
        "${stats.miniAccepted} of ${stats.total} missions taken as mini (${(stats.miniRate * 100).toInt()}%)"
    else
        "${stats.failed + stats.skipped} missed of ${stats.total} — ${(stats.masteryScore * 100).toInt()}% mastery"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundCard, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Filled.Warning,
            contentDescription = null,
            tint = borderColor,
            modifier = Modifier.size(18.dp).padding(top = 2.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = if (isCrutch) "CRUTCH — ${stats.category.displayName.uppercase()}"
                       else "PAIN POINT — ${stats.category.displayName.uppercase()}",
                style = AriseTypography.labelSmall.copy(
                    color = borderColor,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp
                )
            )
            Text(message, style = AriseTypography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp))
            Text(subtitle, style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp))
        }
    }
}

private fun painMessage(category: MissionCategory): String = when (category) {
    MissionCategory.PHYSICAL     -> "Your body is being neglected. The System sees the pattern — avoidance disguised as busyness."
    MissionCategory.MENTAL       -> "Mental gates keep slipping. Sharpness degrades without use. The System does not forget."
    MissionCategory.PRODUCTIVITY -> "Output is low. The gap between intention and execution is widening."
    MissionCategory.SOCIAL       -> "Connection is being deprioritised. Isolation compounds silently."
    MissionCategory.WELLNESS     -> "Recovery is being skipped. The System notes you are running on fumes."
    MissionCategory.CREATIVITY   -> "Creative gates are consistently missed. Rigidity is setting in."
}

private fun crutchMessage(category: MissionCategory): String = when (category) {
    MissionCategory.PHYSICAL     -> "You default to the mini version here. The full gate is within reach — stop negotiating with yourself."
    MissionCategory.MENTAL       -> "You consistently take the easier path on mental missions. Growth requires discomfort."
    MissionCategory.PRODUCTIVITY -> "Mini version accepted too often. The System recommends lowering difficulty instead of cutting corners."
    MissionCategory.SOCIAL       -> "You keep halving your social commitments. Small interactions still count — but consistency matters more."
    MissionCategory.WELLNESS     -> "You accept partial recovery too easily. Rest done properly compounds."
    MissionCategory.CREATIVITY   -> "You shorten creative sessions habitually. Depth requires sustained effort."
}
