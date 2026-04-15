package com.arise.habitquest.domain.usecase

import androidx.test.platform.app.InstrumentationRegistry
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Achievement
import com.arise.habitquest.domain.model.AchievementTrigger
import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.HunterStats
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.model.Rank
import com.arise.habitquest.domain.model.Rarity
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.AchievementRepository
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Instant

class AchievementTriggerValidationTest {

    @Test
    fun categoryAchievement_doesNotUnlockFromTotalMissionCountOnly() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val timeProvider = TimeProvider.getInstance(context)
        val today = timeProvider.sessionDay()

        val missions = buildList {
            repeat(9) { index -> add(completedMission("p$index", MissionCategory.PHYSICAL, today)) }
            add(completedMission("m0", MissionCategory.MENTAL, today))
        }

        val achievementRepo = FakeAchievementRepository(
            listOf(
                achievement(id = "ach_ten_gates", threshold = 10),
                achievement(id = "ach_physical_10", threshold = 10)
            )
        )
        val userRepo = FakeUserRepository(
            profile = UserProfile(
                totalMissionsCompleted = 10,
                xp = 100,
                xpToNextLevel = 100,
                rank = Rank.E,
                level = 1,
                stats = HunterStats()
            )
        )
        val missionRepo = FakeMissionRepository(missions)

        val useCase = UnlockAchievementUseCase(
            achievementRepository = achievementRepo,
            userRepository = userRepo,
            missionRepository = missionRepo,
            timeProvider = timeProvider
        )

        useCase(userRepo.requireProfile())

        assertTrue(achievementRepo.unlockedIds.contains("ach_ten_gates"))
        assertFalse(achievementRepo.unlockedIds.contains("ach_physical_10"))
    }

    @Test
    fun comebackAchievement_requiresPreviousDayWithZeroCompletions() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val timeProvider = TimeProvider.getInstance(context)
        val today = timeProvider.sessionDay()
        val yesterday = today.minusDays(1)

        val missions = listOf(
            completedMission("y_completed", MissionCategory.PHYSICAL, yesterday),
            completedMission("t_completed", MissionCategory.MENTAL, today)
        )

        val achievementRepo = FakeAchievementRepository(
            listOf(achievement(id = "ach_comeback", threshold = 1))
        )
        val userRepo = FakeUserRepository(
            profile = UserProfile(
                totalMissionsCompleted = 2,
                xp = 100,
                xpToNextLevel = 100,
                rank = Rank.E,
                level = 1,
                stats = HunterStats()
            )
        )
        val missionRepo = FakeMissionRepository(missions)

        val useCase = UnlockAchievementUseCase(
            achievementRepository = achievementRepo,
            userRepository = userRepo,
            missionRepository = missionRepo,
            timeProvider = timeProvider
        )

        useCase(userRepo.requireProfile())

        assertFalse(achievementRepo.unlockedIds.contains("ach_comeback"))
    }

    @Test
    fun repairPass_relocksInvalidPreviouslyUnlockedAchievements() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val timeProvider = TimeProvider.getInstance(context)
        val today = timeProvider.sessionDay()

        val missions = buildList {
            repeat(9) { index -> add(completedMission("p$index", MissionCategory.PHYSICAL, today)) }
            add(completedMission("m0", MissionCategory.MENTAL, today))
        }

        val unlockedInvalid = achievement(id = "ach_physical_10", threshold = 10)
            .copy(unlockedAt = Instant.now())

        val achievementRepo = FakeAchievementRepository(
            listOf(
                achievement(id = "ach_ten_gates", threshold = 10),
                unlockedInvalid
            )
        )
        val userRepo = FakeUserRepository(
            profile = UserProfile(
                totalMissionsCompleted = 10,
                xp = 100,
                xpToNextLevel = 100,
                rank = Rank.E,
                level = 1,
                stats = HunterStats()
            )
        )
        val missionRepo = FakeMissionRepository(missions)

        val useCase = UnlockAchievementUseCase(
            achievementRepository = achievementRepo,
            userRepository = userRepo,
            missionRepository = missionRepo,
            timeProvider = timeProvider
        )

        val relocked = useCase.reconcileInvalidUnlocks(userRepo.requireProfile())

        assertTrue(relocked >= 1)
        assertFalse(achievementRepo.unlockedIds.contains("ach_physical_10"))
    }

    @Test
    fun bossDefeatedAchievement_unlocksWhenBossRaidCompleted() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val timeProvider = TimeProvider.getInstance(context)
        val today = timeProvider.sessionDay()

        val missions = listOf(
            completedMission("boss_1", MissionCategory.PHYSICAL, today, type = MissionType.BOSS_RAID)
        )

        val achievementRepo = FakeAchievementRepository(
            listOf(achievement(id = "ach_first_boss", threshold = 1, trigger = AchievementTrigger.BOSS_DEFEATED))
        )
        val userRepo = FakeUserRepository(UserProfile(level = 1, xp = 10, xpToNextLevel = 100, rank = Rank.E))
        val missionRepo = FakeMissionRepository(missions)

        val useCase = UnlockAchievementUseCase(achievementRepo, userRepo, missionRepo, timeProvider)
        useCase(userRepo.requireProfile())

        assertTrue(achievementRepo.unlockedIds.contains("ach_first_boss"))
    }

    @Test
    fun shadowUnlockedAchievement_unlocksFromShadowCount() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val timeProvider = TimeProvider.getInstance(context)
        val today = timeProvider.sessionDay()

        val missions = listOf(
            completedMission("shadow_1", MissionCategory.PHYSICAL, today, streakCount = 21),
            completedMission("shadow_2", MissionCategory.MENTAL, today, streakCount = 22),
            completedMission("shadow_3", MissionCategory.WELLNESS, today, streakCount = 23)
        )

        val achievementRepo = FakeAchievementRepository(
            listOf(achievement(id = "ach_three_shadows", threshold = 3, trigger = AchievementTrigger.SHADOW_UNLOCKED))
        )
        val userRepo = FakeUserRepository(UserProfile(level = 1, xp = 10, xpToNextLevel = 100, rank = Rank.E))
        val missionRepo = FakeMissionRepository(missions)

        val useCase = UnlockAchievementUseCase(achievementRepo, userRepo, missionRepo, timeProvider)
        useCase(userRepo.requireProfile())

        assertTrue(achievementRepo.unlockedIds.contains("ach_three_shadows"))
    }

    @Test
    fun perfectWeekAchievement_unlocksWhenSevenPerfectDaysExist() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val timeProvider = TimeProvider.getInstance(context)
        val today = timeProvider.sessionDay()

        val missions = (0..6).map { idx ->
            completedMission(
                id = "perfect_$idx",
                category = MissionCategory.PHYSICAL,
                dueDate = today.minusDays(idx.toLong())
            )
        }

        val achievementRepo = FakeAchievementRepository(
            listOf(achievement(id = "ach_perfect_week_7", threshold = 1, trigger = AchievementTrigger.PERFECT_WEEK))
        )
        val userRepo = FakeUserRepository(UserProfile(level = 1, xp = 10, xpToNextLevel = 100, rank = Rank.E))
        val missionRepo = FakeMissionRepository(missions)

        val useCase = UnlockAchievementUseCase(achievementRepo, userRepo, missionRepo, timeProvider)
        useCase(userRepo.requireProfile())

        assertTrue(achievementRepo.unlockedIds.contains("ach_perfect_week_7"))
    }

    @Test
    fun hpZeroAchievement_unlocksWhenHpIsZero() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val timeProvider = TimeProvider.getInstance(context)

        val achievementRepo = FakeAchievementRepository(
            listOf(achievement(id = "ach_hp_zero", threshold = 1, trigger = AchievementTrigger.HP_ZERO))
        )
        val userRepo = FakeUserRepository(
            UserProfile(level = 1, xp = 10, xpToNextLevel = 100, rank = Rank.E, hp = 0, maxHp = 100)
        )
        val missionRepo = FakeMissionRepository(emptyList())

        val useCase = UnlockAchievementUseCase(achievementRepo, userRepo, missionRepo, timeProvider)
        useCase(userRepo.requireProfile())

        assertTrue(achievementRepo.unlockedIds.contains("ach_hp_zero"))
    }

    private fun achievement(
        id: String,
        threshold: Int,
        trigger: AchievementTrigger = AchievementTrigger.MISSIONS_COMPLETED
    ): Achievement = Achievement(
        id = id,
        title = id,
        description = id,
        flavorText = id,
        iconName = "star",
        rarity = Rarity.COMMON,
        triggerType = trigger,
        triggerThreshold = threshold,
        progressTarget = threshold
    )

    private fun completedMission(
        id: String,
        category: MissionCategory,
        dueDate: LocalDate,
        type: MissionType = MissionType.DAILY,
        streakCount: Int = 0
    ): Mission = Mission(
        id = id,
        title = id,
        description = id,
        systemLore = id,
        type = type,
        category = category,
        difficulty = Difficulty.C,
        xpReward = 10,
        penaltyXp = 5,
        penaltyHp = 5,
        isCompleted = true,
        dueDate = dueDate,
        streakCount = streakCount
    )

    private class FakeAchievementRepository(initial: List<Achievement>) : AchievementRepository {
        private val achievements = initial.associateBy { it.id }.toMutableMap()
        val unlockedIds = mutableSetOf<String>()

        override fun observeAllAchievements(): Flow<List<Achievement>> = emptyFlow()

        override suspend fun getUnlockedAchievements(): List<Achievement> =
            achievements.values.filter { it.id in unlockedIds }

        override suspend fun getAchievementsByTrigger(trigger: AchievementTrigger): List<Achievement> =
            achievements.values.filter { it.triggerType == trigger }

        override suspend fun insertAchievements(achievements: List<Achievement>) = Unit

        override suspend fun unlockAchievement(id: String) {
            unlockedIds.add(id)
            achievements[id]?.let { ach ->
                achievements[id] = ach.copy(unlockedAt = java.time.Instant.now())
            }
        }

        override suspend fun relockAchievement(id: String) {
            unlockedIds.remove(id)
            achievements[id]?.let { ach ->
                achievements[id] = ach.copy(unlockedAt = null)
            }
        }

        override suspend fun updateProgress(id: String, progress: Int) {
            achievements[id]?.let { ach ->
                achievements[id] = ach.copy(progressCurrent = progress)
            }
        }

        override suspend fun countUnlocked(): Int = unlockedIds.size
    }

    private class FakeMissionRepository(
        missions: List<Mission>
    ) : MissionRepository {
        private val missionMap = missions.associateBy { it.id }.toMutableMap()

        override fun observeMissionsForDate(date: LocalDate): Flow<List<Mission>> = emptyFlow()
        override fun observeWeeklyMissions(weekStart: LocalDate, weekEnd: LocalDate): Flow<List<Mission>> = emptyFlow()
        override fun observeBossRaids(): Flow<List<Mission>> = emptyFlow()
        override fun observePenaltyZone(): Flow<List<Mission>> = emptyFlow()
        override suspend fun getMissionsForDate(date: LocalDate): List<Mission> =
            missionMap.values.filter { it.dueDate == date }

        override suspend fun getMissionById(id: String): Mission? = missionMap[id]
        override suspend fun deleteMissionById(id: String) { missionMap.remove(id) }
        override suspend fun insertMissions(missions: List<Mission>) = missions.forEach { missionMap[it.id] = it }
        override suspend fun insertMission(mission: Mission) { missionMap[mission.id] = mission }
        override suspend fun deleteDailyMissionsForDate(date: LocalDate) = Unit
        override suspend fun failActiveDailyMissionsForDate(date: LocalDate) = Unit
        override suspend fun markCompleted(id: String, streak: Int, usedMini: Boolean) = Unit
        override suspend fun markFailed(id: String) = Unit
        override suspend fun markSkipped(id: String) = Unit
        override suspend fun resetOutcome(id: String) = Unit
        override suspend fun pruneOldDailyMissions(cutoffDate: LocalDate) = Unit
        override suspend fun countCompletedForDate(date: LocalDate): Int =
            missionMap.values.count { it.dueDate == date && it.isCompleted }

        override suspend fun countDailyMissionsForDate(date: LocalDate): Int =
            missionMap.values.count { it.dueDate == date && it.type == MissionType.DAILY }

        override suspend fun getMissionsInRange(from: String, to: String): List<Mission> {
            val start = LocalDate.parse(from)
            val end = LocalDate.parse(to)
            return missionMap.values.filter { !it.dueDate.isBefore(start) && !it.dueDate.isAfter(end) }
        }
    }

    private class FakeUserRepository(
        private var profile: UserProfile
    ) : UserRepository {
        fun requireProfile(): UserProfile = profile

        override fun observeUserProfile(): Flow<UserProfile?> = emptyFlow()
        override suspend fun getUserProfile(): UserProfile? = profile

        override suspend fun upsertProfile(profile: UserProfile) {
            this.profile = profile
        }

        override suspend fun updateXpAndLevel(xp: Long, level: Int, rank: Rank, xpToNext: Long) {
            profile = profile.copy(xp = xp, level = level, rank = rank, xpToNextLevel = xpToNext)
        }

        override suspend fun updateHp(hp: Int) = Unit
        override suspend fun updateStreak(current: Int, best: Int) = Unit
        override suspend fun updateStats(stats: HunterStats) = Unit
        override suspend fun incrementMissionStats(xpGained: Long) = Unit
        override suspend fun decrementMissionStats(completedDelta: Int, xpDelta: Long) = Unit
        override suspend fun updateShields(shields: Int) = Unit
        override suspend fun updateAdaptiveDifficulty(difficulty: Float) = Unit
        override suspend fun incrementDayCount() = Unit
        override suspend fun updateMissState(consecutiveMissDays: Int, pendingWarning: Boolean) = Unit
        override suspend fun getShadowCompletions(templateIds: List<String>): Map<String, Int> = emptyMap()
    }
}
