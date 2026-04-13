package com.arise.habitquest.presentation.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arise.habitquest.domain.model.Stat
import com.arise.habitquest.ui.components.*
import com.arise.habitquest.ui.theme.*
import com.arise.habitquest.ui.components.CategoryRadarChart

@Composable
fun StatusWindowScreen(
    onBack: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    bottomBarPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val categoryStats by viewModel.categoryStats.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDeep,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundSurface)
                    .padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    "STATUS",
                    style = AriseTypography.headlineSmall.copy(letterSpacing = 3.sp)
                )
            }
        }
    ) { padding ->
        profile?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("status_screen")
                    .padding(padding)
                    .padding(bottom = bottomBarPadding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Hero section ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(rankColor(p.rank).copy(0.1f), BackgroundDeep)
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RankBadge(rank = p.rank, size = 72.dp)
                        Text(
                            p.hunterName.uppercase(),
                            style = AriseTypography.headlineMedium.copy(color = TextPrimary)
                        )
                        Text("\"${p.title}\"", style = AriseTypography.bodyMedium.copy(color = TextDim))
                        Text(
                            p.epithet,
                            style = AriseTypography.titleSmall.copy(color = rankColor(p.rank))
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "RANK ${p.rank.displayName}  ·  LEVEL ${p.level}",
                            style = AriseTypography.labelLarge.copy(color = rankColor(p.rank)),
                            modifier = Modifier.testTag("status_rank_level")
                        )
                    }
                }

                Divider(color = BorderDefault, thickness = 1.dp)

                // ── HP/XP ─────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HpBar(current = p.hp, max = p.maxHp, showLabel = true)
                    XpBar(current = p.xp, max = p.xpToNextLevel, showLabel = true)
                }

                Divider(color = BorderDefault)

                // ── Stats ─────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionLabel("HUNTER STATS")
                    Stat.entries.forEach { stat ->
                        StatBar(stat = stat, value = p.stats.get(stat), maxValue = 100)
                    }
                }

                Divider(color = BorderDefault)

                // ── Statistics ────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionLabel("HUNTER RECORD")
                    Spacer(Modifier.height(4.dp))
                    StatRowItem("Days Active", "${p.daysSinceJoin}")
                    StatRowItem("Total Missions", "${p.totalMissionsCompleted}", valueModifier = Modifier.testTag("status_total_missions"))
                    StatRowItem("Total XP Earned", "${p.totalXpEarned}", valueModifier = Modifier.testTag("status_total_xp_earned"))
                    StatRowItem("Current Streak", "${p.streakCurrent} days", valueModifier = Modifier.testTag("status_current_streak"))
                    StatRowItem("Best Streak", "${p.streakBest} days", valueModifier = Modifier.testTag("status_best_streak"))
                    StatRowItem("Streak Shields", "${p.streakShields}")
                    StatRowItem("Member Since", p.joinDate?.toString() ?: "—")
                }

                Divider(color = BorderDefault)

                // ── Life balance radar ────────────────────────────────────
                if (categoryStats.any { it.total > 0 }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionLabel("HUNTER BALANCE — LAST 30 DAYS")
                        CategoryRadarChart(
                            stats = categoryStats,
                            modifier = Modifier.fillMaxWidth()
                        )
                        val worstCategory = categoryStats.filter { it.total >= 3 }.minByOrNull { it.masteryScore }
                        if (worstCategory != null && worstCategory.masteryScore < 0.7f) {
                            Text(
                                text = "WEAK POINT: ${worstCategory.category.displayName.uppercase()} — ${(worstCategory.masteryScore * 100).toInt()}% mastery. Full analysis in Battle Record.",
                                style = AriseTypography.bodySmall.copy(color = TextDim, fontSize = 11.sp)
                            )
                        }
                    }
                    Divider(color = BorderDefault)
                }

                // ── Achievements button ───────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToAchievements,
                        border = BorderStroke(1.dp, BorderAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("status_view_achievements")
                    ) {
                        Icon(Icons.Filled.EmojiEvents, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ACHIEVEMENTS", style = AriseTypography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        border = BorderStroke(1.dp, BorderAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("status_view_history")
                    ) {
                        Icon(Icons.Filled.QueryStats, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("BATTLE RECORD", style = AriseTypography.labelMedium)
                    }
                }
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PurpleCore)
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        label,
        style = AriseTypography.labelSmall.copy(color = TextDim, letterSpacing = 2.sp),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun StatRowItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueModifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = AriseTypography.bodyMedium.copy(color = TextSecondary))
        Text(value, modifier = valueModifier, style = AriseTypography.bodyMedium.copy(color = TextPrimary))
    }
}
