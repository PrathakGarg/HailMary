package com.arise.habitquest.e2e

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.arise.habitquest.MainActivity
import com.arise.habitquest.data.local.database.AppDatabase
import com.arise.habitquest.data.local.database.entity.AchievementEntity
import com.arise.habitquest.data.local.database.entity.MissionEntity
import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.model.Rarity
import com.arise.habitquest.e2e.support.E2EAssertions
import com.arise.habitquest.e2e.support.E2ESelectors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class AchievementRepairPassE2ETest {

    private val resetRule = ResetAppStateRule()

    private val grantNotificationPermission: GrantPermissionRule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        GrantPermissionRule.grant()
    }

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val chain: RuleChain = RuleChain
        .outerRule(resetRule)
        .around(grantNotificationPermission)
        .around(composeRule)

    @Test
    fun startupRepairPass_relocksInvalidCategoryUnlock_andSetsOneTimeFlag() {
        seedRepairScenario(physicalCompletedCount = 9, markCategoryAchievementUnlocked = true)

        E2EAssertions.waitForTag(composeRule, E2ESelectors.BOTTOM_NAV_HOME)

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking {
            assertFalse(E2ETestHarness.isAchievementUnlocked("ach_physical_10", context))
            assertTrue(OnboardingDataStore(context).achievementRepairV1Done.first())
        }
    }

    @Test
    fun startupRepairPass_keepsValidCategoryUnlock() {
        seedRepairScenario(physicalCompletedCount = 10, markCategoryAchievementUnlocked = true)

        E2EAssertions.waitForTag(composeRule, E2ESelectors.BOTTOM_NAV_HOME)

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        runBlocking {
            assertTrue(E2ETestHarness.isAchievementUnlocked("ach_physical_10", context))
            assertTrue(OnboardingDataStore(context).achievementRepairV1Done.first())
        }
    }

    private fun seedRepairScenario(
        physicalCompletedCount: Int,
        markCategoryAchievementUnlocked: Boolean
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getInstance(context)
        val dataStore = OnboardingDataStore(context)
        val sessionDate = com.arise.habitquest.data.time.TimeProvider.getInstance(context).sessionDay().toString()
        val unlockedAt = if (markCategoryAchievementUnlocked) Instant.now().toEpochMilli() else null

        runBlocking {
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("E2E Hunter")
            dataStore.setAchievementRepairV1Done(false)
            dataStore.setLastDailyResetDate(sessionDate)

            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "E2E Hunter",
                    rank = "E",
                    level = 1,
                    xp = 100L,
                    xpToNextLevel = 100L,
                    totalMissionsCompleted = 10,
                    totalXpEarned = 100L,
                    onboardingComplete = true,
                    joinDate = sessionDate
                )
            )

            val missionRows = buildList {
                repeat(physicalCompletedCount) { idx ->
                    add(
                        MissionEntity(
                            id = "repair_phys_$idx",
                            title = "Physical $idx",
                            description = "seed",
                            systemLore = "seed",
                            type = MissionType.DAILY.name,
                            category = MissionCategory.PHYSICAL.name,
                            difficulty = Difficulty.E.name,
                            xpReward = 10,
                            penaltyXp = 5,
                            penaltyHp = 5,
                            isCompleted = true,
                            dueDate = sessionDate,
                            iconName = MissionCategory.PHYSICAL.iconName
                        )
                    )
                }
                add(
                    MissionEntity(
                        id = "repair_mental_0",
                        title = "Mental 0",
                        description = "seed",
                        systemLore = "seed",
                        type = MissionType.DAILY.name,
                        category = MissionCategory.MENTAL.name,
                        difficulty = Difficulty.E.name,
                        xpReward = 10,
                        penaltyXp = 5,
                        penaltyHp = 5,
                        isCompleted = true,
                        dueDate = sessionDate,
                        iconName = MissionCategory.MENTAL.iconName
                    )
                )
            }
            db.missionDao().insertMissions(missionRows)

            db.achievementDao().insertAchievements(
                listOf(
                    AchievementEntity(
                        id = "ach_ten_gates",
                        title = "Gate Runner",
                        description = "Complete 10 missions total.",
                        flavorText = "seed",
                        iconName = "star_half",
                        rarity = Rarity.COMMON.name,
                        unlockedAt = Instant.now().toEpochMilli(),
                        progressCurrent = 10,
                        progressTarget = 10,
                        xpBonus = 100,
                        triggerType = "MISSIONS_COMPLETED",
                        triggerThreshold = 10
                    ),
                    AchievementEntity(
                        id = "ach_physical_10",
                        title = "Iron in the Blood",
                        description = "Complete 10 Physical missions.",
                        flavorText = "seed",
                        iconName = "fitness_center",
                        rarity = Rarity.COMMON.name,
                        unlockedAt = unlockedAt,
                        progressCurrent = 10,
                        progressTarget = 10,
                        xpBonus = 120,
                        triggerType = "MISSIONS_COMPLETED",
                        triggerThreshold = 10
                    )
                )
            )

            val physicalCompleted = db.missionDao().getMissionsForDate(sessionDate)
                .count { it.category == MissionCategory.PHYSICAL.name && it.isCompleted }
            assertEquals(physicalCompletedCount, physicalCompleted)
        }
    }
}
