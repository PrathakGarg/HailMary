package com.arise.habitquest.data.mapper

import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import com.arise.habitquest.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class UserProfileMapperTest {

    private val mapper = UserProfileMapper()

    @Test
    fun toDomain_mapsProgressionProfileFields() {
        val entity = UserProfileEntity(
            trackFocus = "WELLNESS",
            equipmentMode = "MIXED",
            scheduleStyle = "FLEXIBLE_SPLIT",
            shoulderRiskFlag = true,
            heatRiskFlag = true,
            progressionPreference = "CONSERVATIVE",
            progressionState = "HOLD",
            transitionRecommendation = "MENTAL",
            joinDate = "2026-04-16"
        )

        val profile = mapper.toDomain(entity)

        assertEquals(MissionCategory.WELLNESS, profile.trackFocus)
        assertEquals(EquipmentMode.MIXED, profile.equipmentMode)
        assertEquals(ScheduleStyle.FLEXIBLE_SPLIT, profile.scheduleStyle)
        assertTrue(profile.shoulderRiskFlag)
        assertTrue(profile.heatRiskFlag)
        assertEquals(ProgressionPreference.CONSERVATIVE, profile.progressionPreference)
        assertEquals(ProgressionState.HOLD, profile.progressionState)
        assertEquals(MissionCategory.MENTAL, profile.transitionRecommendation)
        assertEquals(LocalDate.of(2026, 4, 16), profile.joinDate)
    }

    @Test
    fun toDomain_fallsBackForUnknownEnumValues() {
        val entity = UserProfileEntity(
            trackFocus = "UNKNOWN_TRACK",
            equipmentMode = "UNKNOWN_EQUIPMENT",
            scheduleStyle = "UNKNOWN_SCHEDULE",
            progressionPreference = "UNKNOWN_PREF",
            progressionState = "UNKNOWN_STATE",
            transitionRecommendation = ""
        )

        val profile = mapper.toDomain(entity)

        assertEquals(MissionCategory.PHYSICAL, profile.trackFocus)
        assertEquals(EquipmentMode.BODYWEIGHT, profile.equipmentMode)
        assertEquals(ScheduleStyle.FIXED_WINDOW, profile.scheduleStyle)
        assertEquals(ProgressionPreference.ASSERTIVE_SAFE, profile.progressionPreference)
        assertEquals(ProgressionState.PROGRESSING, profile.progressionState)
        assertEquals(null, profile.transitionRecommendation)
    }
}
