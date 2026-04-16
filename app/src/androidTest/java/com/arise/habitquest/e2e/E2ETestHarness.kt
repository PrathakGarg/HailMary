package com.arise.habitquest.e2e

import android.content.Context
import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import com.arise.habitquest.data.local.database.AppDatabase
import com.arise.habitquest.data.local.database.entity.AchievementEntity
import com.arise.habitquest.data.local.database.entity.DailyLogEntity
import com.arise.habitquest.data.local.database.entity.MissionEntity
import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.mapper.MissionMapper
import com.arise.habitquest.data.mapper.UserProfileMapper
import com.arise.habitquest.data.repository.MissionRepositoryImpl
import com.arise.habitquest.data.repository.UserRepositoryImpl
import com.arise.habitquest.data.generator.MissionGenerator
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Rarity
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.usecase.ApplyDailyResetUseCase
import com.arise.habitquest.domain.usecase.GenerateDailyMissionsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

object E2ETestHarness {

    fun targetContext(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    fun resetToFreshInstallState(context: Context = targetContext()) {
        val dataStore = OnboardingDataStore(context)
        val db = AppDatabase.getInstance(context)

        runBlocking {
            dataStore.setOnboardingComplete(false)
            dataStore.setHunterName("")
            dataStore.setLastDailyResetDate("")
            dataStore.setNotificationHour(8)
            dataStore.setFocusThemes(emptySet())
            dataStore.setPendingRankUp("")
            dataStore.setLastMonthlyReport("")
            dataStore.setDayStartMinutes(TimeProvider.DEFAULT_DAY_START_MINUTES)
            db.clearAllTables()
            check(!dataStore.onboardingComplete.first())
        }

        context.getSharedPreferences("arise_time_anchor", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    fun setOnboardingComplete(value: Boolean, context: Context = targetContext()) {
        runBlocking {
            OnboardingDataStore(context).setOnboardingComplete(value)
        }
    }

    fun seedReturningUserWithActiveDailyMission(
        missionId: String = "e2e_daily_1",
        missionTitle: String = "E2E Daily Gate",
        context: Context = targetContext()
    ) {
        val dataStore = OnboardingDataStore(context)
        val db = AppDatabase.getInstance(context)
        val sessionDate = TimeProvider.getInstance(context).sessionDay().toString()

        runBlocking {
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("E2E Hunter")
            dataStore.setNotificationHour(8)
            dataStore.setLastDailyResetDate(sessionDate)

            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "E2E Hunter",
                    epithet = "Silent Fierce Relentless",
                    title = "The Unawakened",
                    rank = "E",
                    level = 1,
                    xp = 10L,
                    xpToNextLevel = 100L,
                    onboardingComplete = true,
                    joinDate = sessionDate
                )
            )

            db.missionDao().insertMission(
                MissionEntity(
                    id = missionId,
                    title = missionTitle,
                    description = "Complete the seeded E2E mission.",
                    systemLore = "[E2E] Deterministic mission seeded for instrumentation.",
                    miniMissionDescription = "Do a reduced pass of the seeded E2E mission.",
                    type = MissionType.DAILY.name,
                    category = MissionCategory.PHYSICAL.name,
                    difficulty = Difficulty.E.name,
                    xpReward = 25,
                    penaltyXp = 5,
                    penaltyHp = 5,
                    statRewardsJson = JSONObject(mapOf("STR" to 1, "VIT" to 1)).toString(),
                    dueDate = sessionDate,
                    scheduledTimeHint = "MORNING",
                    iconName = MissionCategory.PHYSICAL.iconName
                )
            )
        }
    }

    fun seedProgressionScenario(
        missionId: String = "e2e_progress_daily",
        context: Context = targetContext()
    ) {
        val dataStore = OnboardingDataStore(context)
        val db = AppDatabase.getInstance(context)
        val sessionDate = TimeProvider.getInstance(context).sessionDay().toString()

        runBlocking {
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("E2E Hunter")
            dataStore.setNotificationHour(8)
            dataStore.setLastDailyResetDate(sessionDate)

            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "E2E Hunter",
                    epithet = "Silent Fierce Relentless",
                    title = "The Unawakened",
                    rank = "E",
                    level = 1,
                    xp = 90L,
                    xpToNextLevel = 100L,
                    hp = 100,
                    maxHp = 100,
                    streakCurrent = 2,
                    streakBest = 2,
                    daysSinceJoin = 3,
                    totalMissionsCompleted = 0,
                    totalXpEarned = 0L,
                    onboardingComplete = true,
                    joinDate = sessionDate
                )
            )

            db.achievementDao().insertAchievements(
                listOf(
                    AchievementEntity(
                        id = "ach_first_gate",
                        title = "First Gate",
                        description = "Complete your first mission.",
                        flavorText = "The system recognizes your first step.",
                        iconName = "ic_achievement_first_gate",
                        rarity = Rarity.COMMON.name,
                        progressTarget = 1,
                        xpBonus = 50,
                        triggerType = "MISSIONS_COMPLETED",
                        triggerThreshold = 1
                    ),
                    AchievementEntity(
                        id = "ach_streak_3",
                        title = "Three-Day Hunter",
                        description = "Reach a 3-day streak.",
                        flavorText = "Consistency begins to sharpen the blade.",
                        iconName = "ic_achievement_streak_3",
                        rarity = Rarity.RARE.name,
                        progressCurrent = 2,
                        progressTarget = 3,
                        xpBonus = 75,
                        triggerType = "STREAK_DAYS",
                        triggerThreshold = 3
                    ),
                    AchievementEntity(
                        id = "ach_level_10",
                        title = "Double Digits",
                        description = "Reach level 10.",
                        flavorText = "A far-off milestone.",
                        iconName = "ic_achievement_level_10",
                        rarity = Rarity.EPIC.name,
                        progressCurrent = 1,
                        progressTarget = 10,
                        xpBonus = 200,
                        triggerType = "LEVEL_REACHED",
                        triggerThreshold = 10
                    )
                )
            )

            db.missionDao().insertMission(
                MissionEntity(
                    id = missionId,
                    title = "Threshold Gate",
                    description = "Complete the progression seed mission.",
                    systemLore = "[E2E] Progression scenario seeded for deterministic validation.",
                    miniMissionDescription = "Complete a shortened pass of the threshold gate.",
                    type = MissionType.DAILY.name,
                    category = MissionCategory.MENTAL.name,
                    difficulty = Difficulty.E.name,
                    xpReward = 20,
                    penaltyXp = 5,
                    penaltyHp = 5,
                    statRewardsJson = JSONObject(mapOf("INT" to 1, "SENSE" to 1)).toString(),
                    dueDate = sessionDate,
                    scheduledTimeHint = "MORNING",
                    iconName = MissionCategory.MENTAL.iconName
                )
            )
        }
    }

    fun seedRankUpScenario(
        missionId: String = "e2e_rankup_daily",
        context: Context = targetContext()
    ) {
        val dataStore = OnboardingDataStore(context)
        val db = AppDatabase.getInstance(context)
        val sessionDate = TimeProvider.getInstance(context).sessionDay().toString()

        runBlocking {
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("E2E Hunter")
            dataStore.setNotificationHour(8)
            dataStore.setLastDailyResetDate(sessionDate)
            dataStore.setPendingRankUp("")

            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "E2E Hunter",
                    epithet = "Silent Fierce Relentless",
                    title = "The Unawakened",
                    rank = "E",
                    level = 100,
                    xp = 9_950L,
                    xpToNextLevel = 10_000L,
                    hp = 100,
                    maxHp = 100,
                    streakCurrent = 2,
                    streakBest = 2,
                    daysSinceJoin = 30,
                    totalMissionsCompleted = 0,
                    totalXpEarned = 0L,
                    onboardingComplete = true,
                    joinDate = sessionDate
                )
            )

            db.achievementDao().insertAchievements(
                listOf(
                    AchievementEntity(
                        id = "ach_first_gate",
                        title = "First Gate",
                        description = "Complete your first mission.",
                        flavorText = "The system recognizes your first step.",
                        iconName = "ic_achievement_first_gate",
                        rarity = Rarity.COMMON.name,
                        progressTarget = 1,
                        xpBonus = 50,
                        triggerType = "MISSIONS_COMPLETED",
                        triggerThreshold = 1
                    ),
                    AchievementEntity(
                        id = "ach_streak_3",
                        title = "Three-Day Hunter",
                        description = "Reach a 3-day streak.",
                        flavorText = "Consistency begins to sharpen the blade.",
                        iconName = "ic_achievement_streak_3",
                        rarity = Rarity.RARE.name,
                        progressCurrent = 2,
                        progressTarget = 3,
                        xpBonus = 75,
                        triggerType = "STREAK_DAYS",
                        triggerThreshold = 3
                    )
                )
            )

            db.missionDao().insertMission(
                MissionEntity(
                    id = missionId,
                    title = "Awakening Gate",
                    description = "Complete the seeded rank-up mission.",
                    systemLore = "[E2E] Rank-up scenario seeded for deterministic validation.",
                    miniMissionDescription = "Complete a shortened pass of the awakening gate.",
                    type = MissionType.DAILY.name,
                    category = MissionCategory.WELLNESS.name,
                    difficulty = Difficulty.E.name,
                    xpReward = 20,
                    penaltyXp = 5,
                    penaltyHp = 5,
                    statRewardsJson = JSONObject(mapOf("INT" to 1, "SENSE" to 1)).toString(),
                    dueDate = sessionDate,
                    scheduledTimeHint = "MORNING",
                    iconName = MissionCategory.WELLNESS.iconName
                )
            )
        }
    }

    fun seedHistoryScenario(context: Context = targetContext()) {
        val dataStore = OnboardingDataStore(context)
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()

        runBlocking {
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("E2E Hunter")
            dataStore.setNotificationHour(8)
            dataStore.setLastDailyResetDate(today.toString())

            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "E2E Hunter",
                    epithet = "Silent Fierce Relentless",
                    title = "The Unawakened",
                    rank = "E",
                    level = 2,
                    xp = 45L,
                    xpToNextLevel = 200L,
                    streakCurrent = 2,
                    streakBest = 4,
                    daysSinceJoin = 6,
                    totalMissionsCompleted = 5,
                    totalXpEarned = 185L,
                    onboardingComplete = true,
                    joinDate = today.minusDays(5).toString()
                )
            )

            listOf(
                DailyLogEntity(
                    date = today.minusDays(2).toString(),
                    completedIdsJson = "[\"hist_1\",\"hist_2\"]",
                    xpGained = 70,
                    rankSnapshot = "E",
                    levelSnapshot = 1,
                    completionRate = 1.0f,
                    totalMissions = 2,
                    systemMessage = "Perfect execution."
                ),
                DailyLogEntity(
                    date = today.minusDays(1).toString(),
                    completedIdsJson = "[\"hist_3\"]",
                    failedIdsJson = "[\"hist_4\"]",
                    xpGained = 40,
                    xpLost = 5,
                    hpLost = 10,
                    rankSnapshot = "E",
                    levelSnapshot = 2,
                    completionRate = 0.5f,
                    totalMissions = 2,
                    systemMessage = "Partial success."
                ),
                DailyLogEntity(
                    date = today.toString(),
                    completedIdsJson = "[\"hist_5\"]",
                    xpGained = 80,
                    rankSnapshot = "E",
                    levelSnapshot = 2,
                    completionRate = 1.0f,
                    totalMissions = 1,
                    systemMessage = "Momentum maintained."
                )
            ).forEach { db.dailyLogDao().upsertLog(it) }
        }
    }

    fun seedHistoryScenarioWithoutTodayLog(context: Context = targetContext()) {
        val dataStore = OnboardingDataStore(context)
        val db = AppDatabase.getInstance(context)
        val today = TimeProvider.getInstance(context).sessionDay()

        runBlocking {
            dataStore.setOnboardingComplete(true)
            dataStore.setHunterName("E2E Hunter")
            dataStore.setNotificationHour(8)
            dataStore.setLastDailyResetDate(today.toString())

            db.userProfileDao().upsertProfile(
                UserProfileEntity(
                    hunterName = "E2E Hunter",
                    epithet = "Silent Fierce Relentless",
                    title = "The Unawakened",
                    rank = "E",
                    level = 2,
                    xp = 45L,
                    xpToNextLevel = 200L,
                    streakCurrent = 1,
                    streakBest = 4,
                    daysSinceJoin = 6,
                    totalMissionsCompleted = 4,
                    totalXpEarned = 140L,
                    onboardingComplete = true,
                    joinDate = today.minusDays(5).toString()
                )
            )

            listOf(
                DailyLogEntity(
                    date = today.minusDays(4).toString(),
                    xpGained = 35,
                    rankSnapshot = "E",
                    levelSnapshot = 1,
                    completionRate = 0.5f,
                    totalMissions = 2,
                    systemMessage = "Recovered."
                ),
                DailyLogEntity(
                    date = today.minusDays(3).toString(),
                    xpGained = 50,
                    rankSnapshot = "E",
                    levelSnapshot = 1,
                    completionRate = 0.75f,
                    totalMissions = 2,
                    systemMessage = "Stable."
                ),
                DailyLogEntity(
                    date = today.minusDays(2).toString(),
                    xpGained = 70,
                    rankSnapshot = "E",
                    levelSnapshot = 2,
                    completionRate = 1.0f,
                    totalMissions = 2,
                    systemMessage = "Perfect execution."
                ),
                DailyLogEntity(
                    date = today.minusDays(1).toString(),
                    xpGained = 30,
                    rankSnapshot = "E",
                    levelSnapshot = 2,
                    completionRate = 0.25f,
                    totalMissions = 2,
                    systemMessage = "Partial success."
                )
            ).forEach { db.dailyLogDao().upsertLog(it) }
        }
    }

    fun getUserProfileEntity(context: Context = targetContext()): UserProfileEntity? {
        val db = AppDatabase.getInstance(context)
        return runBlocking { db.userProfileDao().getUserProfile() }
    }

    fun getUnlockedAchievementCount(context: Context = targetContext()): Int {
        val db = AppDatabase.getInstance(context)
        return runBlocking { db.achievementDao().countUnlocked() }
    }

    fun isAchievementUnlocked(achievementId: String, context: Context = targetContext()): Boolean {
        val db = AppDatabase.getInstance(context)
        return runBlocking {
            db.achievementDao().getUnlockedAchievements().any { it.id == achievementId }
        }
    }

    fun isMissionCompleted(missionId: String, context: Context = targetContext()): Boolean {
        val db = AppDatabase.getInstance(context)
        return runBlocking {
            db.missionDao().getMissionById(missionId)?.isCompleted == true
        }
    }

    fun isMissionFailed(missionId: String, context: Context = targetContext()): Boolean {
        val db = AppDatabase.getInstance(context)
        return runBlocking {
            db.missionDao().getMissionById(missionId)?.isFailed == true
        }
    }

    fun pinTrustedTime(
        localDateTime: LocalDateTime,
        dayStartMinutes: Int = TimeProvider.DEFAULT_DAY_START_MINUTES,
        context: Context = targetContext()
    ) {
        val timeProvider = TimeProvider.getInstance(context)
        val anchorElapsedMs = SystemClock.elapsedRealtime()
        val anchorNetworkMs = localDateTime
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        context.getSharedPreferences("arise_time_anchor", Context.MODE_PRIVATE)
            .edit()
            .putLong("network_ms", anchorNetworkMs)
            .putLong("elapsed_ms", anchorElapsedMs)
            .putInt("day_start_minutes", dayStartMinutes)
            .putInt("reset_hour", dayStartMinutes / 60)
            .putInt("reset_minute", dayStartMinutes % 60)
            .commit()

        timeProvider.setDayStartMinutes(dayStartMinutes)

        val clazz = timeProvider.javaClass
        clazz.getDeclaredField("anchorNetworkMs").apply {
            isAccessible = true
            setLong(timeProvider, anchorNetworkMs)
        }
        clazz.getDeclaredField("anchorElapsedMs").apply {
            isAccessible = true
            setLong(timeProvider, anchorElapsedMs)
        }
    }

    fun getSessionDay(context: Context = targetContext()): LocalDate =
        TimeProvider.getInstance(context).sessionDay()

    fun getMinutesUntilReset(context: Context = targetContext()): Long =
        TimeProvider.getInstance(context).minutesUntilReset()

    fun applyDailyReset(context: Context = targetContext()) {
        val db = AppDatabase.getInstance(context)
        val timeProvider = TimeProvider.getInstance(context)
        val dataStore = OnboardingDataStore(context)
        val generator = MissionGenerator(timeProvider)
        val missionRepository = MissionRepositoryImpl(db.missionDao(), MissionMapper(), timeProvider, context)
        val userRepository = UserRepositoryImpl(db.userProfileDao(), db.shadowDao(), UserProfileMapper())
        val generateDailyMissions = GenerateDailyMissionsUseCase(
            missionRepository = missionRepository,
            userRepository = userRepository,
            generator = generator,
            dataStore = dataStore
        )
        val useCase = ApplyDailyResetUseCase(
            userRepository = userRepository,
            missionRepository = missionRepository,
            dailyLogDao = db.dailyLogDao(),
            generateDailyMissions = generateDailyMissions,
            generator = generator,
            dataStore = dataStore,
            timeProvider = timeProvider
        )
        runBlocking {
            useCase.invoke()
        }
    }

    fun upsertUserProfile(profile: UserProfileEntity, context: Context = targetContext()) {
        val db = AppDatabase.getInstance(context)
        runBlocking {
            db.userProfileDao().upsertProfile(profile)
        }
    }

    fun insertMission(mission: MissionEntity, context: Context = targetContext()) {
        val db = AppDatabase.getInstance(context)
        runBlocking {
            db.missionDao().insertMission(mission)
        }
    }

    fun getMissionsForDate(date: LocalDate, context: Context = targetContext()): List<MissionEntity> {
        val db = AppDatabase.getInstance(context)
        return runBlocking {
            db.missionDao().getMissionsForDate(date.toString())
        }
    }

    fun getDailyLog(date: LocalDate, context: Context = targetContext()): DailyLogEntity? {
        val db = AppDatabase.getInstance(context)
        return runBlocking {
            db.dailyLogDao().getLogForDate(date.toString())
        }
    }

    fun getLastDailyResetDate(context: Context = targetContext()): String = runBlocking {
        OnboardingDataStore(context).lastDailyResetDate.first()
    }

    fun getLastMonthlyReport(context: Context = targetContext()): String = runBlocking {
        OnboardingDataStore(context).lastMonthlyReport.first()
    }

    fun setFocusThemes(themes: Set<String>, context: Context = targetContext()) {
        runBlocking {
            OnboardingDataStore(context).setFocusThemes(themes)
        }
    }
}
