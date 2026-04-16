package com.arise.habitquest.data.generator

import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.PhysicalMissionFamily
import com.arise.habitquest.domain.model.UserProfile
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MissionGeneratorSelectionTest {

    private val generator = MissionGenerator(mockk<TimeProvider>(relaxed = true))
    private val profile = UserProfile(hunterName = "test-hunter")
    private val date = LocalDate.of(2026, 4, 16)

    private val physicalBaseTemplateIds = listOf(
        MissionTemplates.pushUps.id,
        MissionTemplates.squat.id,
        MissionTemplates.morningRun.id,
        MissionTemplates.plank.id,
        MissionTemplates.stretching.id
    )

    private val wellnessTemplateIds = MissionTemplates
        .byCategory(MissionCategory.WELLNESS)
        .map { it.id }
        .toSet()

    @Test
    fun selectDailyTemplateIds_prioritizesUnderFloorFamily() {
        val selection = generator.selectDailyTemplateIds(
            profile = profile,
            baseTemplateIds = physicalBaseTemplateIds,
            goalCategories = setOf(MissionCategory.PHYSICAL),
            date = date,
            recentTemplateIds = emptySet(),
            weekToDateFamilyCounts = mapOf(
                PhysicalMissionFamily.PUSH to 1,
                PhysicalMissionFamily.LOWER_BODY to 1,
                PhysicalMissionFamily.CARDIO_NEAT to 1,
                PhysicalMissionFamily.CORE_STABILITY to 0,
                PhysicalMissionFamily.MOBILITY_PREHAB to 1
            ),
            excludedTemplateIds = wellnessTemplateIds
        )

        val selectedFamilies = selection.allIds
            .mapNotNull { id -> MissionTemplates.all.find { it.id == id } }
            .map { it.physicalFamily }

        assertTrue(
            "Expected at least one CORE_STABILITY template when weekly floor is under target.",
            selectedFamilies.contains(PhysicalMissionFamily.CORE_STABILITY)
        )
    }

    @Test
    fun selectDailyTemplateIds_prioritizesHighMissFamilyWithinSameBand() {
        val selection = generator.selectDailyTemplateIds(
            profile = profile,
            baseTemplateIds = physicalBaseTemplateIds,
            goalCategories = setOf(MissionCategory.PHYSICAL),
            date = date,
            recentTemplateIds = emptySet(),
            weekToDateFamilyCounts = mapOf(
                PhysicalMissionFamily.PUSH to 1,
                PhysicalMissionFamily.LOWER_BODY to 1,
                PhysicalMissionFamily.CARDIO_NEAT to 1,
                PhysicalMissionFamily.CORE_STABILITY to 1,
                PhysicalMissionFamily.MOBILITY_PREHAB to 1
            ),
            weekToDateFamilyMissCounts = mapOf(
                PhysicalMissionFamily.LOWER_BODY to 3,
                PhysicalMissionFamily.PUSH to 0,
                PhysicalMissionFamily.CARDIO_NEAT to 0,
                PhysicalMissionFamily.CORE_STABILITY to 0,
                PhysicalMissionFamily.MOBILITY_PREHAB to 0
            ),
            excludedTemplateIds = wellnessTemplateIds
        )

        val firstRotatingFamily = selection.rotatingIds
            .mapNotNull { id -> MissionTemplates.all.find { it.id == id } }
            .firstOrNull { it.category == MissionCategory.PHYSICAL }
            ?.physicalFamily

        assertEquals(
            "Expected highest-miss family to be prioritized first when floors are already satisfied.",
            PhysicalMissionFamily.LOWER_BODY,
            firstRotatingFamily
        )
    }

    @Test
    fun selectDailyTemplateIds_respectsSafetyThrottleCapsForCardioAndFullBody() {
        val selection = generator.selectDailyTemplateIds(
            profile = profile,
            baseTemplateIds = physicalBaseTemplateIds,
            goalCategories = setOf(MissionCategory.PHYSICAL),
            date = date,
            recentTemplateIds = emptySet(),
            weekToDateFamilyCounts = mapOf(
                PhysicalMissionFamily.PUSH to 1,
                PhysicalMissionFamily.LOWER_BODY to 1,
                PhysicalMissionFamily.CARDIO_NEAT to 2,
                PhysicalMissionFamily.CORE_STABILITY to 1,
                PhysicalMissionFamily.MOBILITY_PREHAB to 1,
                PhysicalMissionFamily.FULL_BODY to 1
            ),
            safetyThrottle = true,
            excludedTemplateIds = wellnessTemplateIds
        )

        val rotatingFamilies = selection.rotatingIds
            .mapNotNull { id -> MissionTemplates.all.find { it.id == id } }
            .filter { it.category == MissionCategory.PHYSICAL }
            .map { it.physicalFamily }

        assertTrue(
            "Expected no CARDIO_NEAT selection when safety throttle cardio cap is reached.",
            PhysicalMissionFamily.CARDIO_NEAT !in rotatingFamilies
        )
        assertTrue(
            "Expected no FULL_BODY selection when safety throttle full-body cap is reached.",
            PhysicalMissionFamily.FULL_BODY !in rotatingFamilies
        )
    }

    @Test
    fun selectDailyTemplateIds_productivityTrack_prioritizesProductivityWhenUnderFloor() {
        val productivityProfile = profile.copy(trackFocus = MissionCategory.PRODUCTIVITY)
        val baseTemplateIds = listOf(
            MissionTemplates.pushUps.id,
            MissionTemplates.deepWork.id,
            MissionTemplates.taskList.id,
            MissionTemplates.hydration.id,
            MissionTemplates.meditation.id
        )

        val selection = generator.selectDailyTemplateIds(
            profile = productivityProfile,
            baseTemplateIds = baseTemplateIds,
            goalCategories = setOf(MissionCategory.PHYSICAL, MissionCategory.PRODUCTIVITY),
            date = date,
            recentTemplateIds = emptySet(),
            weekToDateCategoryCounts = mapOf(
                MissionCategory.PRODUCTIVITY to 0,
                MissionCategory.WELLNESS to 1
            )
        )

        val selectedCategories = selection.rotatingIds
            .mapNotNull { id -> MissionTemplates.all.find { it.id == id } }
            .map { it.category }

        assertTrue(
            "Expected PRODUCTIVITY rotation when productivity track is below weekly floor.",
            selectedCategories.contains(MissionCategory.PRODUCTIVITY)
        )
    }

    @Test
    fun selectDailyTemplateIds_respectsWeeklyCapsForSocialAndCreativity() {
        val socialCreativeBaseTemplateIds = listOf(
            MissionTemplates.pushUps.id,
            MissionTemplates.reachOut.id,
            MissionTemplates.kindness.id,
            MissionTemplates.creative.id,
            MissionTemplates.brainstorm.id
        )

        val selection = generator.selectDailyTemplateIds(
            profile = profile,
            baseTemplateIds = socialCreativeBaseTemplateIds,
            goalCategories = setOf(MissionCategory.PHYSICAL, MissionCategory.SOCIAL, MissionCategory.CREATIVITY),
            date = date,
            recentTemplateIds = emptySet(),
            weekToDateCategoryCounts = mapOf(
                MissionCategory.SOCIAL to 1,
                MissionCategory.CREATIVITY to 1
            )
        )

        val selectedCategories = selection.rotatingIds
            .mapNotNull { id -> MissionTemplates.all.find { it.id == id } }
            .map { it.category }

        assertTrue(
            "Expected no SOCIAL rotation when weekly SOCIAL cap is already reached.",
            MissionCategory.SOCIAL !in selectedCategories
        )
        assertTrue(
            "Expected no CREATIVITY rotation when weekly CREATIVITY cap is already reached.",
            MissionCategory.CREATIVITY !in selectedCategories
        )
    }
}
