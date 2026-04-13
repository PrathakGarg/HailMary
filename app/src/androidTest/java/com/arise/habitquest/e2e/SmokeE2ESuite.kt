package com.arise.habitquest.e2e

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    LaunchRoutingE2ETest::class,
    ReturningUserLaunchE2ETest::class,
    BottomNavSmokeE2ETest::class,
    OnboardingNavBarSmokeE2ETest::class,
    OnboardingNavigationE2ETest::class,
    MissionBoardNavigationE2ETest::class
)
class SmokeE2ESuite