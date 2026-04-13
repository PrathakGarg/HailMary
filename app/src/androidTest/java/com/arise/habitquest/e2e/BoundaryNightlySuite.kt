package com.arise.habitquest.e2e

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    RegressionMatrixNightlyTest::class,
    DayBoundaryNightlyTest::class,
    WorkerNightlyTest::class,
    SettingsBoundaryNightlyTest::class,
    SettingsActionsNightlyTest::class,
    SettingsUiNightlyTest::class,
    NotificationWorkerNightlyTest::class,
    TamperResistanceNightlyTest::class
)
class BoundaryNightlySuite
