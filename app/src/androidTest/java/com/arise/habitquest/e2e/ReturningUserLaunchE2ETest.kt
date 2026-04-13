package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors
import com.arise.habitquest.e2e.support.E2EStateSeeds
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReturningUserLaunchE2ETest {

    private val resetRule = ResetAppStateRule()

    private val seedReturningUserRule = object : TestWatcher() {
        override fun starting(description: Description) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            E2EStateSeeds.seedReturningUser(context)
        }
    }

    private val grantNotificationPermission: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(resetRule)
        .around(seedReturningUserRule)
        .around(grantNotificationPermission)
        .around(composeRule)

    @Test
    fun returningUser_launchRoutesToHomeTabs() {
        E2EAssertions.waitForTag(composeRule, E2ESelectors.BOTTOM_NAV_HOME)
        E2EAssertions.assertDisplayed(composeRule, E2ESelectors.BOTTOM_NAV_HOME)
        E2EAssertions.assertDisplayed(composeRule, E2ESelectors.BOTTOM_NAV_MISSIONS)
    }
}
