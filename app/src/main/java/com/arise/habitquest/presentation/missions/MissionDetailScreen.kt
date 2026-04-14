package com.arise.habitquest.presentation.missions

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
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

@Composable
fun MissionDetailScreen(
    missionId: String,
    onComplete: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: MissionDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionCompleted) {
        if (state.actionCompleted && state.completionResult != null) {
            onComplete(missionId)
        } else if (state.actionCompleted && state.failResult != null) {
            onBack()
        }
    }

    LaunchedEffect(state.deprioritizeReplaced) {
        if (state.deprioritizeReplaced) {
            onBack()
        }
    }

    LaunchedEffect(state.deprioritizeError) {
        if (state.deprioritizeError) {
            snackbarHostState.showSnackbar("Could not replace this mission right now.")
        }
    }

    val mission = state.mission ?: return
    val catColor = categoryColor(mission.category)

    Scaffold(
        containerColor = BackgroundDeep,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(catColor.copy(0.08f), BackgroundDeep)))
                    .padding(top = 44.dp, start = 8.dp, end = 16.dp, bottom = 8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = TextSecondary)
                }
            }
        },
        bottomBar = {
            if (mission.isActive) {
                MissionActionBar(
                    onComplete = viewModel::complete,
                    onFail = viewModel::fail,
                    onDeprioritize = viewModel::deprioritizeAndReplace,
                    useMini = state.useMiniVersion,
                    onToggleMini = viewModel::toggleMiniVersion,
                    hasMini = mission.miniMissionDescription.isNotBlank()
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("mission_detail_screen")
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category icon + type badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(catColor.copy(alpha = 0.15f))
                        .border(1.dp, catColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                ) {
                    Icon(categoryIcon(mission.category), null, tint = catColor, modifier = Modifier.size(28.dp))
                }
                Column {
                    Text(
                        "${mission.type.displayName}  ·  ${mission.difficulty.displayName}-RANK",
                        style = AriseTypography.labelSmall.copy(color = TextDim, letterSpacing = 1.5.sp, fontSize = 10.sp)
                    )
                    Text(mission.title, style = AriseTypography.titleLarge)
                }
            }

            // System lore card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundCard)
                    .border(1.dp, PurpleCore.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("[ SYSTEM ]", style = AriseTypography.labelSmall.copy(color = PurpleLight, fontSize = 9.sp))
                    Spacer(Modifier.height(6.dp))
                    Text(mission.systemLore, style = SystemTextStyle)
                }
            }

            // Description
            if (state.useMiniVersion && mission.miniMissionDescription.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldCore.copy(alpha = 0.08f))
                        .border(1.dp, GoldCore.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("MINI MISSION", style = AriseTypography.labelSmall.copy(color = GoldCore, fontSize = 9.sp))
                        Text(mission.miniMissionDescription, style = AriseTypography.bodyMedium)
                        Text("50% XP reward", style = AriseTypography.labelSmall.copy(color = GoldCore, fontSize = 10.sp))
                    }
                }
            } else {
                Text(mission.description, style = AriseTypography.bodyMedium)
            }

            // Rewards section
            SectionLabel("REWARDS")
            RewardCard(xpReward = mission.effectiveXpReward, statRewards = mission.statRewards)

            // Consequences
            if (!mission.isCompleted) {
                SectionLabel("CONSEQUENCES OF FAILURE")
                ConsequenceCard(penaltyXp = mission.penaltyXp, penaltyHp = mission.penaltyHp)
            }

            // Streak
            if (mission.streakCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.LocalFireDepartment, null, tint = GoldCore, modifier = Modifier.size(18.dp))
                    Text(
                        "Current streak: ${mission.streakCount} days",
                        style = AriseTypography.bodyMedium.copy(color = GoldCore)
                    )
                }
            }
        }
    }
}

@Composable
fun RewardCard(xpReward: Int, statRewards: Map<com.arise.habitquest.domain.model.Stat, Int>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EmeraldCore.copy(alpha = 0.06f))
            .border(1.dp, EmeraldCore.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Star, null, tint = GoldCore, modifier = Modifier.size(18.dp))
                Text("+$xpReward XP", style = AriseTypography.titleMedium.copy(color = GoldCore))
            }
            statRewards.entries.forEach { (stat, amount) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(statColor(stat))
                    )
                    Text("+$amount ${stat.name}", style = AriseTypography.bodySmall.copy(color = statColor(stat)))
                }
            }
        }
    }
}

@Composable
fun ConsequenceCard(penaltyXp: Int, penaltyHp: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CrimsonCore.copy(alpha = 0.06f))
            .border(1.dp, CrimsonCore.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Warning, null, tint = CrimsonCore, modifier = Modifier.size(16.dp))
                Text("-$penaltyXp XP  ·  -$penaltyHp HP", style = AriseTypography.bodyMedium.copy(color = CrimsonLight))
            }
            Text(
                "Grace period applies on first miss. Streak will reset.",
                style = AriseTypography.bodySmall.copy(color = TextDim)
            )
        }
    }
}

@Composable
fun MissionActionBar(
    onComplete: () -> Unit,
    onFail: () -> Unit,
    onDeprioritize: () -> Unit,
    useMini: Boolean,
    onToggleMini: () -> Unit,
    hasMini: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundSurface)
            .border(BorderStroke(1.dp, BorderDefault), RoundedCornerShape(0.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (hasMini) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Use mini version (50% XP)", style = AriseTypography.bodySmall.copy(color = TextSecondary))
                Switch(
                    checked = useMini,
                    onCheckedChange = { onToggleMini() },
                    colors = SwitchDefaults.colors(checkedThumbColor = GoldCore, checkedTrackColor = GoldDim)
                )
            }
        }
        TextButton(
            onClick = onDeprioritize,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mission_detail_deprioritize")
        ) {
            Icon(Icons.Filled.Tune, null, modifier = Modifier.size(16.dp), tint = TextSecondary)
            Spacer(Modifier.width(6.dp))
            Text(
                "DEPRIORITIZE THIS MISSION TYPE",
                style = AriseTypography.labelMedium.copy(color = TextSecondary)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onFail,
                border = BorderStroke(1.dp, CrimsonCore.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonLight),
                modifier = Modifier
                    .weight(1f)
                    .testTag("mission_detail_fail")
            ) {
                Icon(Icons.Filled.Close, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("FAIL", style = AriseTypography.labelMedium)
            }
            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldCore),
                modifier = Modifier
                    .weight(2f)
                    .testTag("mission_detail_complete")
                    .glowEffect(EmeraldCore.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("GATE CLEARED", style = AriseTypography.labelLarge)
            }
        }
    }
}
