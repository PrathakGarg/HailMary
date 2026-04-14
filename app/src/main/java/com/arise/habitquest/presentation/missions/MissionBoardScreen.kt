package com.arise.habitquest.presentation.missions

import androidx.compose.foundation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.ui.components.*
import com.arise.habitquest.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MissionBoardScreen(
    onMissionClick: (String) -> Unit,
    onBack: () -> Unit = {},
    bottomBarPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(),
    viewModel: MissionBoardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("DAILY", "WEEKLY", "BOSS RAID", "PENALTY ZONE")
    val pagerState = rememberPagerState(initialPage = state.selectedTab, pageCount = { tabs.size })

    LaunchedEffect(state.selectedTab) {
        if (pagerState.currentPage != state.selectedTab) {
            pagerState.animateScrollToPage(state.selectedTab)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> viewModel.selectTab(page) }
    }

    Scaffold(
        containerColor = BackgroundDeep,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundSurface)
                    .padding(top = 44.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    "MISSION BOARD",
                    style = AriseTypography.headlineSmall.copy(letterSpacing = 3.sp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = bottomBarPadding.calculateBottomPadding())
        ) {
            // Tab row
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = BackgroundSurface,
                contentColor = PurpleCore,
                modifier = Modifier.testTag("missions_tab_row"),
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = PurpleCore
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { viewModel.selectTab(index) },
                        modifier = Modifier.testTag("missions_tab_$index"),
                        text = {
                            Text(
                                title,
                                style = AriseTypography.labelSmall.copy(
                                    color = if (pagerState.currentPage == index) PurpleCore else TextDim,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .testTag("missions_pager")
            ) { page ->
                val missions = when (page) {
                    0 -> state.dailyMissions
                    1 -> state.weeklyMissions
                    2 -> state.bossRaids
                    3 -> state.penaltyZone
                    else -> emptyList()
                }

                MissionListPage(
                    missions = missions,
                    selectedTab = page,
                    isLoading = state.isLoading,
                    onMissionClick = onMissionClick,
                    onResetMission = viewModel::resetMissionOutcome
                )
            }
        }
    }
}

@Composable
private fun MissionListPage(
    missions: List<Mission>,
    selectedTab: Int,
    isLoading: Boolean,
    onMissionClick: (String) -> Unit,
    onResetMission: (String) -> Unit
) {
    if (missions.isEmpty() && !isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("missions_empty_page_$selectedTab"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when (selectedTab) {
                    3 -> "No penalty zone. The System is... satisfied."
                    else -> "No missions in this category."
                }
                ,
                style = SystemTextStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(40.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("missions_list_page_$selectedTab"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val active = missions.filter { it.isActive }
            val done = missions.filter { !it.isActive }

            if (active.isNotEmpty()) {
                item {
                    SectionLabel("ACTIVE (${active.size})")
                }
                items(active) { mission ->
                    MissionCard(mission = mission, onClick = { onMissionClick(mission.id) })
                }
            }
            if (done.isNotEmpty()) {
                item { SectionLabel("COMPLETED / FAILED") }
                items(done) { mission ->
                    MissionCard(
                        mission = mission,
                        onClick = { onMissionClick(mission.id) },
                        onReset = { onResetMission(mission.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionLabel(label: String) {
    Text(
        label,
        style = AriseTypography.labelSmall.copy(color = TextDim, letterSpacing = 2.sp),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
