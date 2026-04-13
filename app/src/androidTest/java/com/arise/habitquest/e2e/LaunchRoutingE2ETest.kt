package com.arise.habitquest.e2e

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LaunchRoutingE2ETest {

    @get:Rule
    val loggingRule = E2ETestLoggingRule()

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @get:Rule
    val grantNotificationPermission: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    @Test
    fun freshInstall_launchRoutesToOnboarding() {
        E2ETestHarness.resetToFreshInstallState()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)

        E2EAssertions.waitForTag(composeRule, E2ESelectors.ONBOARDING_NEXT)
        E2EAssertions.assertDisplayed(composeRule, E2ESelectors.ONBOARDING_NEXT)
    }
}
