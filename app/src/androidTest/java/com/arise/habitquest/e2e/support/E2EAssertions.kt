package com.arise.habitquest.e2e.support

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag

object E2EAssertions {

    fun waitForTag(
        rule: ComposeTestRule,
        tag: String,
        timeoutMillis: Long = 12_000
    ) {
        rule.waitUntil(timeoutMillis = timeoutMillis) {
            rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    fun assertDisplayed(rule: ComposeTestRule, tag: String) {
        rule.onNodeWithTag(tag).assertIsDisplayed()
    }

    fun waitForDisplayedTag(
        rule: ComposeTestRule,
        tag: String,
        timeoutMillis: Long = 12_000
    ) {
        rule.waitUntil(timeoutMillis = timeoutMillis) {
            runCatching {
                rule.onNodeWithTag(tag).assertIsDisplayed()
            }.isSuccess
        }
    }
}
