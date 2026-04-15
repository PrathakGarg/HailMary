package com.arise.habitquest.presentation.achievements

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arise.habitquest.domain.model.Achievement
import com.arise.habitquest.domain.model.Rarity
import com.arise.habitquest.ui.components.AriseTopBar
import com.arise.habitquest.ui.components.glowEffect
import com.arise.habitquest.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AchievementScreen(
    onBack: () -> Unit,
    bottomBarPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(),
    viewModel: AchievementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }

    Scaffold(
        containerColor = BackgroundDeep,
        topBar = {
            AriseTopBar(
                title = "ACHIEVEMENTS",
                trailingContent = {
                    Text(
                        "${state.unlockedCount}/${state.totalCount}",
                        style = AriseTypography.labelMedium.copy(color = GoldCore),
                        modifier = Modifier.testTag("achievement_summary")
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("achievement_screen")
                .padding(padding)
                .padding(bottom = bottomBarPadding.calculateBottomPadding())
        ) {
            // Filter chips
            ScrollableFilterRow(
                filters = AchievementFilter.entries,
                selected = state.selectedFilter,
                onSelect = viewModel::setFilter
            )

            if (state.filteredAchievements.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No achievements in this category yet.", style = SystemTextStyle)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredAchievements) { achievement ->
                        AchievementCard(
                            achievement = achievement,
                            onClick = { selectedAchievement = achievement },
                            modifier = Modifier.testTag("achievement_card_${achievement.id}")
                        )
                    }
                }
            }
        }
    }

    selectedAchievement?.let { achievement ->
        AchievementDetailDialog(
            achievement = achievement,
            onDismiss = { selectedAchievement = null }
        )
    }
}

@Composable
private fun AchievementDetailDialog(achievement: Achievement, onDismiss: () -> Unit) {
    val rarityColor = rarityColor(achievement.rarity)
    val isUnlocked = achievement.isUnlocked

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("achievement_detail_dialog"),
        containerColor = BackgroundSurface,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    achievement.rarity.displayName.uppercase(),
                    style = AriseTypography.labelSmall.copy(
                        color = rarityColor,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    if (isUnlocked) achievement.title else "???",
                    style = AriseTypography.titleMedium.copy(color = TextPrimary)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isUnlocked) {
                    Text(
                        achievement.description,
                        style = AriseTypography.bodyMedium.copy(color = TextPrimary)
                    )
                    if (achievement.flavorText.isNotBlank()) {
                        Text(
                            "\"${achievement.flavorText}\"",
                            style = AriseTypography.bodySmall.copy(
                                color = TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                    achievement.unlockedAt?.let { instant ->
                        val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                        Text(
                            "Unlocked: $date",
                            style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 10.sp)
                        )
                    }
                } else {
                    Text(
                        "Complete the required objective to unlock this achievement.",
                        style = AriseTypography.bodyMedium.copy(color = TextSecondary)
                    )
                    if (achievement.progressTarget > 1) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = achievement.progressPercent,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
                                color = rarityColor,
                                trackColor = BackgroundElevated
                            )
                            Text(
                                "Progress: ${achievement.progressCurrent} / ${achievement.progressTarget}",
                                style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 10.sp)
                            )
                        }
                    }
                }
                // XP bonus — always visible
                if (achievement.xpBonus > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldCore.copy(alpha = 0.12f))
                            .border(1.dp, GoldCore.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Star, null,
                                tint = GoldCore,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                if (isUnlocked) "${achievement.xpBonus} XP awarded"
                                else "+${achievement.xpBonus} XP on unlock",
                                style = AriseTypography.labelMedium.copy(color = GoldCore)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", style = AriseTypography.labelMedium.copy(color = PurpleCore))
            }
        }
    )
}

@Composable
fun ScrollableFilterRow(
    filters: List<AchievementFilter>,
    selected: AchievementFilter,
    onSelect: (AchievementFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .testTag("achievement_filter_${filter.name.lowercase()}")
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) PurpleCore else BackgroundCard)
                    .border(1.dp, if (isSelected) PurpleCore else BorderDefault, RoundedCornerShape(20.dp))
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    filter.label,
                    style = AriseTypography.labelSmall.copy(
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val rarityColor = rarityColor(achievement.rarity)
    val isUnlocked = achievement.isUnlocked

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BackgroundCard)
            .clickable(onClick = onClick)
            .border(
                1.5.dp,
                if (isUnlocked) rarityColor else BorderDefault,
                RoundedCornerShape(14.dp)
            )
            .then(if (isUnlocked) Modifier.glowEffect(rarityColor.copy(alpha = 0.25f), 6.dp) else Modifier)
            .padding(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = if (!isUnlocked) Modifier.blur(if (!isUnlocked) 3.dp else 0.dp) else Modifier
        ) {
            // Icon placeholder (using text emoji as icon since Material doesn't have all)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(rarityColor.copy(alpha = if (isUnlocked) 0.2f else 0.05f))
            ) {
                if (isUnlocked) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        null,
                        tint = rarityColor,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Text("?", style = AriseTypography.headlineSmall.copy(color = TextDim))
                }
            }

            // Rarity tag
            Text(
                achievement.rarity.displayName.uppercase(),
                style = AriseTypography.labelSmall.copy(
                    color = rarityColor,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp
                )
            )

            Text(
                if (isUnlocked) achievement.title else "???",
                style = AriseTypography.titleSmall.copy(
                    color = if (isUnlocked) TextPrimary else TextDim
                ),
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (isUnlocked) {
                Text(
                    achievement.description,
                    style = AriseTypography.bodySmall.copy(color = TextPrimary),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                if (achievement.xpBonus > 0) {
                    Text(
                        "+${achievement.xpBonus} XP",
                        style = AriseTypography.labelSmall.copy(
                            color = GoldCore,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
                achievement.unlockedAt?.let { instant ->
                    val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                    Text(
                        date.toString(),
                        style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp)
                    )
                }
            } else {
                // Progress bar for locked achievements
                if (achievement.progressTarget > 1) {
                    LinearProgressIndicator(
                        progress = achievement.progressPercent,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
                        color = rarityColor.copy(alpha = 0.5f),
                        trackColor = BackgroundElevated
                    )
                    Text(
                        "${achievement.progressCurrent}/${achievement.progressTarget}",
                        style = AriseTypography.labelSmall.copy(color = TextDim, fontSize = 9.sp)
                    )
                }
            }
        }
    }
}

fun rarityColor(rarity: Rarity): Color = when (rarity) {
    Rarity.COMMON -> RankColorE
    Rarity.RARE -> BlueCore
    Rarity.EPIC -> StatSenseColor
    Rarity.LEGENDARY -> GoldCore
    Rarity.MYTHIC -> RankColorSS
}
