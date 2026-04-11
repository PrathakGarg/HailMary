package com.arise.habitquest.presentation.history

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arise.habitquest.domain.model.MissionCategory
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
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundSurface)
                    .padding(top = 44.dp, start = 8.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = TextSecondary)
                    }
                    Text("BATTLE RECORD", style = AriseTypography.headlineSmall.copy(letterSpacing = 3.sp))
                }
            }
        }
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
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Summary stats row ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatCard("MISSIONS", "${state.totalMissionsCompleted}", Modifier.weight(1f))
                SummaryStatCard("TOTAL XP", "${state.totalXpEarned}", Modifier.weight(1f))
                SummaryStatCard("BEST STREAK", "${state.bestStreak}d", Modifier.weight(1f))
            }

            // ── Calendar heatmap ─────────────────────────────────────────────
            SectionHeader("GATE HISTORY — LAST 90 DAYS")
            CalendarHeatmap(days = state.calendarDays, today = state.today)

            // ── Legend ───────────────────────────────────────────────────────
            HeatmapLegend()

            // ── XP bar chart (weekly) ────────────────────────────────────────
            if (state.weeklyXp.any { it.second > 0 }) {
                SectionHeader("WEEKLY XP EARNED")
                WeeklyXpChart(weeks = state.weeklyXp)
            }

            // ── Analysis insights ─────────────────────────────────────────────
            if (state.insights.isNotEmpty()) {
                SectionHeader("SYSTEM ANALYSIS")
                state.insights.forEach { insight ->
                    InsightCard(insight = insight)
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
private fun CalendarHeatmap(days: List<DayEntry>, today: LocalDate) {
    // 13 weeks × 7 days grid
    val weeks = days.chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        // Day-of-week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Spacer(Modifier.width(28.dp))
            listOf("M","T","W","T","F","S","S").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp),
                    textAlign = TextAlign.Center
                )
            }
        }

        weeks.forEachIndexed { weekIndex, week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Week label (month abbrev on first day of month)
                val firstOfWeek = week.firstOrNull()?.date
                val weekLabel = if (firstOfWeek?.dayOfMonth == 1 || weekIndex == 0) {
                    firstOfWeek?.month?.getDisplayName(TextStyle.SHORT, Locale.getDefault())?.take(3) ?: ""
                } else ""
                Text(
                    weekLabel,
                    modifier = Modifier.width(28.dp),
                    style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 8.sp)
                )

                // Pad if week has fewer than 7 days
                val paddedWeek = week + List(7 - week.size) { null }
                paddedWeek.forEach { entry ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    entry == null -> Color.Transparent
                                    !entry.hasData -> BackgroundElevated
                                    entry.date == today -> PurpleCore
                                    entry.completionRate >= 1.0f -> EmeraldCore
                                    entry.completionRate >= 0.75f -> EmeraldCore.copy(alpha = 0.7f)
                                    entry.completionRate >= 0.5f -> GoldCore.copy(alpha = 0.6f)
                                    entry.completionRate > 0f -> CrimsonCore.copy(alpha = 0.5f)
                                    else -> BackgroundElevated
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun HeatmapLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Less", style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp))
        listOf(BackgroundElevated, CrimsonCore.copy(0.5f), GoldCore.copy(0.6f), EmeraldCore.copy(0.7f), EmeraldCore).forEach { color ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
        Text("More", style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp))
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(PurpleCore))
        Text("Today", style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp))
    }
}

@Composable
private fun WeeklyXpChart(weeks: List<Pair<String, Long>>) {
    val maxXp = weeks.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1L
    Row(
        modifier = Modifier
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
private fun InsightCard(insight: AnalysisInsight) {
    Row(
        modifier = Modifier
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
