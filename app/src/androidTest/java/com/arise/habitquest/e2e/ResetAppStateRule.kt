package com.arise.habitquest.e2e

import org.junit.runner.Description

class ResetAppStateRule : E2ETestLoggingRule() {
    override fun starting(description: Description) {
        super.starting(description)
        E2ETestHarness.resetToFreshInstallState()
    }
}
