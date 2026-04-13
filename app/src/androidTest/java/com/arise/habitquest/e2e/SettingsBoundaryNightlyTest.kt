package com.arise.habitquest.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arise.habitquest.data.time.TimeProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Settings-boundary integration tests — nightly bucket.
 *
 * Each test pins a specific wall-clock time and then changes the
 * [TimeProvider.dayStartMinutes] setting, verifying how that live change
 * propagates to session-day resolution and the minutes-until-reset countdown.
 *
 * These complement the raw boundary assertions in [DayBoundaryNightlyTest] by
 * exercising the full user-facing settings path rather than the default boundary.
 */
@RunWith(AndroidJUnit4::class)
class SettingsBoundaryNightlyTest {

    private val resetRule = ResetAppStateRule()

    @get:Rule
    val chain: RuleChain = RuleChain.outerRule(resetRule)

    // ── 1. Shifting the boundary later ───────────────────────────────────────

    /**
     * A hunter currently at 5:15 AM — after the default 4:30 boundary, so session
     * day = today.  If they push the day-start to 6:30 AM the same clock time is
     * now BEFORE the new boundary, so session day should flip to yesterday.
     */
    @Test
    fun dayStartMinutes_shiftedLate_movesSessionDayToYesterday() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val anchored = LocalDateTime.of(2026, 4, 12, 5, 15) // 5:15 AM

        // Default 4:30 boundary (270 min): 5:15 > 4:30 → sessionDay = today
        E2ETestHarness.pinTrustedTime(localDateTime = anchored, dayStartMinutes = 270, context = context)
        assertEquals(LocalDate.of(2026, 4, 12), E2ETestHarness.getSessionDay(context))

        // Push boundary to 6:30 AM (390 min): 5:15 < 6:30 → sessionDay = yesterday
        E2ETestHarness.pinTrustedTime(localDateTime = anchored, dayStartMinutes = 390, context = context)
        assertEquals(LocalDate.of(2026, 4, 11), E2ETestHarness.getSessionDay(context))
    }

    // ── 2. Shifting the boundary earlier ─────────────────────────────────────

    /**
     * A hunter at 4:00 AM — before the default 4:30 boundary, so session day =
     * yesterday.  If they move the day-start to 3:30 AM the same clock time is now
     * AFTER the new boundary, so session day should flip to today.
     */
    @Test
    fun dayStartMinutes_shiftedEarly_movesSessionDayToToday() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val anchored = LocalDateTime.of(2026, 4, 12, 4, 0) // 4:00 AM

        // Default 4:30 boundary (270 min): 4:00 < 4:30 → sessionDay = yesterday
        E2ETestHarness.pinTrustedTime(localDateTime = anchored, dayStartMinutes = 270, context = context)
        assertEquals(LocalDate.of(2026, 4, 11), E2ETestHarness.getSessionDay(context))

        // Pull boundary to 3:30 AM (210 min): 4:00 > 3:30 → sessionDay = today
        E2ETestHarness.pinTrustedTime(localDateTime = anchored, dayStartMinutes = 210, context = context)
        assertEquals(LocalDate.of(2026, 4, 12), E2ETestHarness.getSessionDay(context))
    }

    // ── 3. Countdown changes proportionally with the boundary ─────────────────

    /**
     * With the wall clock at 8:00 AM the countdown to the next reset should be
     * longer when the boundary is pushed from 4:30 AM to 6:00 AM — because the
     * gate closes 90 minutes later in that scenario.
     */
    @Test
    fun dayStartMinutes_laterBoundary_increasesMinutesUntilResetCountdown() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val anchored = LocalDateTime.of(2026, 4, 12, 8, 0) // 8:00 AM

        // ── With default 4:30 boundary ────────────────────────────────────────
        // Next reset: 2026-04-13 04:30 → 20 h 30 min = 1230 min away
        E2ETestHarness.pinTrustedTime(localDateTime = anchored, dayStartMinutes = 270, context = context)
        val minutesAt430 = E2ETestHarness.getMinutesUntilReset(context)
        assertTrue(
            "Expected ~1230 minutes until 4:30 reset, got $minutesAt430",
            minutesAt430 in 1228L..1232L
        )

        // ── Shift boundary to 6:00 AM ─────────────────────────────────────────
        // Next reset: 2026-04-13 06:00 → 22 h = 1320 min away (90 min more)
        E2ETestHarness.pinTrustedTime(localDateTime = anchored, dayStartMinutes = 360, context = context)
        val minutesAt600 = E2ETestHarness.getMinutesUntilReset(context)
        assertTrue(
            "Expected ~1320 minutes until 6:00 reset, got $minutesAt600",
            minutesAt600 in 1318L..1322L
        )

        assertTrue(
            "Later boundary ($minutesAt600 min) should give a longer countdown than earlier boundary ($minutesAt430 min)",
            minutesAt600 > minutesAt430
        )
    }
}
