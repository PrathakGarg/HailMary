package com.arise.habitquest.e2e

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    OnboardingFlowE2ETest::class,
    RouteRegressionE2ETest::class,
    MissionDetailFlowE2ETest::class,
    HomeMissionBoardSyncE2ETest::class,
    BottomNavBackStackE2ETest::class,
    ProgressionStatusAchievementsE2ETest::class,
    HistoryE2ETest::class,
    RankUpFlowE2ETest::class
)
class FullFunctionalE2ESuite