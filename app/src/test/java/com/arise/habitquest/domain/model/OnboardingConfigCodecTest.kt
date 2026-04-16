package com.arise.habitquest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class OnboardingConfigCodecTest {

    @Test
    fun encodeDecode_preservesExpandedBaselineContext() {
        val json = OnboardingConfigCodec.encode(
            templateIds = listOf("push-ups", "plank"),
            restDay = DayOfWeek.SUNDAY,
            startingDifficulty = StartingDifficulty.HARD,
            goals = setOf(Goal.FITNESS, Goal.MENTAL_HEALTH),
            fitnessLevel = FitnessLevel.MODERATE,
            sleepQuality = SleepQuality.GOOD,
            stressLevel = StressLevel.HIGH,
            workHoursPerDay = 10,
            availableTime = AvailableTime.SCATTERED,
            failureResponse = FailureResponse.NEED_TIME,
            accountabilityStyle = AccountabilityStyle.BALANCED,
            longestStreak = LongestStreak.ONE_TO_FOUR_WEEKS,
            failureReasons = setOf(FailureReason.BURNOUT, FailureReason.TOO_BUSY),
            progressionPreference = ProgressionPreference.AGGRESSIVE,
            scheduleStyle = ScheduleStyle.FLEXIBLE_SPLIT,
            equipmentMode = EquipmentMode.MIXED,
            trackFocus = MissionCategory.PHYSICAL,
            shoulderRiskFlag = true,
            heatRiskFlag = true
        )

        val decoded = OnboardingConfigCodec.decode(json)

        assertEquals(listOf("push-ups", "plank"), decoded.templateIds)
        assertTrue(decoded.goalCategories.contains(MissionCategory.PHYSICAL))
        assertTrue(decoded.goalCategories.contains(MissionCategory.WELLNESS))
        assertEquals(ProgressionPreference.AGGRESSIVE, decoded.progressionPreference)
        assertEquals(ScheduleStyle.FLEXIBLE_SPLIT, decoded.scheduleStyle)
        assertEquals(EquipmentMode.MIXED, decoded.equipmentMode)
        assertEquals(MissionCategory.PHYSICAL, decoded.trackFocus)
        assertTrue(decoded.shoulderRiskFlag)
        assertTrue(decoded.heatRiskFlag)
        assertEquals(FitnessLevel.MODERATE, decoded.fitnessLevel)
        assertEquals(SleepQuality.GOOD, decoded.sleepQuality)
        assertEquals(StressLevel.HIGH, decoded.stressLevel)
        assertEquals(10, decoded.workHoursPerDay)
        assertEquals(AvailableTime.SCATTERED, decoded.availableTime)
        assertEquals(FailureResponse.NEED_TIME, decoded.failureResponse)
        assertEquals(AccountabilityStyle.BALANCED, decoded.accountabilityStyle)
        assertEquals(LongestStreak.ONE_TO_FOUR_WEEKS, decoded.longestStreak)
        assertTrue(decoded.failureReasons.contains(FailureReason.BURNOUT))
        assertTrue(decoded.failureReasons.contains(FailureReason.TOO_BUSY))
    }

    @Test
    fun decode_invalidJson_returnsSafeDefaults() {
        val decoded = OnboardingConfigCodec.decode("not-json")

        assertTrue(decoded.templateIds.isEmpty())
        assertTrue(decoded.goalCategories.isEmpty())
        assertEquals(ProgressionPreference.ASSERTIVE_SAFE, decoded.progressionPreference)
        assertEquals(ScheduleStyle.FIXED_WINDOW, decoded.scheduleStyle)
        assertEquals(EquipmentMode.BODYWEIGHT, decoded.equipmentMode)
        assertEquals(MissionCategory.PHYSICAL, decoded.trackFocus)
        assertFalse(decoded.shoulderRiskFlag)
        assertFalse(decoded.heatRiskFlag)
    }
}
