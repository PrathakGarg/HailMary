package com.arise.habitquest.domain.usecase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.domain.model.Difficulty
import com.arise.habitquest.domain.model.HunterStats
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.model.MissionCategory
import com.arise.habitquest.domain.model.MissionRollbackEntry
import com.arise.habitquest.domain.model.MissionType
import com.arise.habitquest.domain.model.Rank
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.MissionRepository
import com.arise.habitquest.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RollbackLedgerAndResetUseCaseTest {

    private lateinit var dataStore: OnboardingDataStore

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataStore = OnboardingDataStore(context)
        dataStore.clearMissionRollbackLedger()
    }

    @After
    fun tearDown() = runBlocking {
        dataStore.clearMissionRollbackLedger()
    }

    @Test
    fun resetOlderMission_doesNotRollbackLaterMissionChanges() = runBlocking {
        val profile = UserProfile(
            xp = 30,
            hp = 110,
            maxHp = 200,
            stats = HunterStats(str = 8, agi = 10, int = 5, vit = 5, end = 5, sense = 5),
            totalMissionsCompleted = 2,
            totalXpEarned = 30
        )
        val older = mission(id = "m_old", isCompleted = true, xpReward = 10, statStr = 3)
        val later = mission(id = "m_new", isCompleted = true, xpReward = 20, statAgi = 5)

        val missionRepo = FakeMissionRepository(listOf(older, later))
        val userRepo = FakeUserRepository(profile)
        val useCase = ResetMissionOutcomeUseCase(missionRepo, userRepo, dataStore)

        dataStore.setMissionRollbackEntry(
            "m_old",
            MissionRollbackEntry(
                recordedAtMillis = System.currentTimeMillis(),
                xpDelta = 10,
                hpDelta = 5,
                strDelta = 3,
                agiDelta = 0,
                intDelta = 0,
                vitDelta = 0,
                endDelta = 0,
                senseDelta = 0,
                missionCountDelta = 1,
                totalXpEarnedDelta = 10
            )
        )
        dataStore.setMissionRollbackEntry(
            "m_new",
            MissionRollbackEntry(
                recordedAtMillis = System.currentTimeMillis(),
                xpDelta = 20,
                hpDelta = 5,
                strDelta = 0,
                agiDelta = 5,
                intDelta = 0,
                vitDelta = 0,
                endDelta = 0,
                senseDelta = 0,
                missionCountDelta = 1,
                totalXpEarnedDelta = 20
            )
        )

        val reset = useCase("m_old")
        assertTrue(reset)

        val updated = userRepo.profile
        assertEquals(20L, updated.xp)
        assertEquals(105, updated.hp)
        assertEquals(5, updated.stats.str)
        assertEquals(10, updated.stats.agi)
        assertEquals(1, updated.totalMissionsCompleted)
        assertEquals(20L, updated.totalXpEarned)

        val ledgerAfter = dataStore.missionRollbackLedger.first()
        assertTrue("m_old" !in ledgerAfter)
        assertTrue("m_new" in ledgerAfter)
    }

    @Test
    fun resetFailedMission_restoresPenaltyOnly() = runBlocking {
        val profile = UserProfile(
            xp = 40,
            hp = 82,
            maxHp = 100,
            stats = HunterStats(),
            totalMissionsCompleted = 0,
            totalXpEarned = 0
        )
        val failed = mission(id = "m_fail", isFailed = true, penaltyXp = 8, penaltyHp = 12)

        val missionRepo = FakeMissionRepository(listOf(failed))
        val userRepo = FakeUserRepository(profile)
        val useCase = ResetMissionOutcomeUseCase(missionRepo, userRepo, dataStore)

        dataStore.setMissionRollbackEntry(
            "m_fail",
            MissionRollbackEntry(
                recordedAtMillis = System.currentTimeMillis(),
                xpDelta = -8,
                hpDelta = -12,
                strDelta = 0,
                agiDelta = 0,
                intDelta = 0,
                vitDelta = 0,
                endDelta = 0,
                senseDelta = 0,
                missionCountDelta = 0,
                totalXpEarnedDelta = 0
            )
        )

        val reset = useCase("m_fail")
        assertTrue(reset)

        val updated = userRepo.profile
        assertEquals(48L, updated.xp)
        assertEquals(94, updated.hp)
    }

    @Test
    fun tamperedLedgerCannotGrantXpOnReset() = runBlocking {
        val profile = UserProfile(
            xp = 35,
            hp = 88,
            maxHp = 100,
            stats = HunterStats(str = 7, agi = 5, int = 5, vit = 5, end = 5, sense = 5),
            totalMissionsCompleted = 1,
            totalXpEarned = 10
        )
        val completed = mission(id = "m_tamper", isCompleted = true, xpReward = 10, statStr = 2)

        val missionRepo = FakeMissionRepository(listOf(completed))
        val userRepo = FakeUserRepository(profile)
        val useCase = ResetMissionOutcomeUseCase(missionRepo, userRepo, dataStore)

        // Tampered payload attempts to increase XP/HP when reset is pressed.
        dataStore.setMissionRollbackEntry(
            "m_tamper",
            MissionRollbackEntry(
                recordedAtMillis = System.currentTimeMillis(),
                xpDelta = -9999,
                hpDelta = -999,
                strDelta = -50,
                agiDelta = 0,
                intDelta = 0,
                vitDelta = 0,
                endDelta = 0,
                senseDelta = 0,
                missionCountDelta = -9,
                totalXpEarnedDelta = -9999
            )
        )

        val reset = useCase("m_tamper")
        assertTrue(reset)

        val updated = userRepo.profile
        assertEquals(25L, updated.xp)
        assertEquals(83, updated.hp)
        assertEquals(5, updated.stats.str)
        assertEquals(0, updated.totalMissionsCompleted)
        assertEquals(0L, updated.totalXpEarned)
    }

    @Test
    fun rollbackLedgerPrunesOldAndExcessEntries() = runBlocking {
        val now = System.currentTimeMillis()
        val oldTs = now - (30L * 24 * 60 * 60 * 1000)

        dataStore.setMissionRollbackEntry(
            "old_entry",
            MissionRollbackEntry(
                recordedAtMillis = oldTs,
                xpDelta = 1,
                hpDelta = 1,
                strDelta = 0,
                agiDelta = 0,
                intDelta = 0,
                vitDelta = 0,
                endDelta = 0,
                senseDelta = 0,
                missionCountDelta = 1,
                totalXpEarnedDelta = 1
            )
        )

        repeat(520) { i ->
            dataStore.setMissionRollbackEntry(
                "recent_$i",
                MissionRollbackEntry(
                    recordedAtMillis = now + i,
                    xpDelta = 1,
                    hpDelta = 1,
                    strDelta = 0,
                    agiDelta = 0,
                    intDelta = 0,
                    vitDelta = 0,
                    endDelta = 0,
                    senseDelta = 0,
                    missionCountDelta = 1,
                    totalXpEarnedDelta = 1
                )
            )
        }

        dataStore.pruneMissionRollbackLedger(now)
        val ledger = dataStore.missionRollbackLedger.first()

        assertTrue("old_entry" !in ledger)
        assertTrue(ledger.size <= 500)
    }

    @Test
    fun resetCompletedMissionWithoutLedgerEntry_doesNotCrash_andResetsMission() = runBlocking {
        val profile = UserProfile(
            xp = 18,
            hp = 95,
            maxHp = 100,
            stats = HunterStats(str = 6, agi = 5, int = 5, vit = 5, end = 5, sense = 5),
            totalMissionsCompleted = 1,
            totalXpEarned = 8
        )
        val completedLegacy = mission(id = "m_legacy", isCompleted = true, xpReward = 8, statStr = 1)

        val missionRepo = FakeMissionRepository(listOf(completedLegacy))
        val userRepo = FakeUserRepository(profile)
        val useCase = ResetMissionOutcomeUseCase(missionRepo, userRepo, dataStore)

        // No rollback entry intentionally: mission completed before ledger feature existed.
        val reset = useCase("m_legacy")
        assertTrue(reset)

        val missionAfter = missionRepo.getMissionById("m_legacy")
        assertTrue(missionAfter != null)
        assertTrue(missionAfter?.isActive == true)
    }

    private fun mission(
        id: String,
        isCompleted: Boolean = false,
        isFailed: Boolean = false,
        xpReward: Int = 10,
        penaltyXp: Int = 8,
        penaltyHp: Int = 10,
        statStr: Int = 0,
        statAgi: Int = 0
    ): Mission = Mission(
        id = id,
        title = "Test Mission $id",
        description = "desc",
        systemLore = "lore",
        miniMissionDescription = "",
        type = MissionType.DAILY,
        category = MissionCategory.PRODUCTIVITY,
        difficulty = Difficulty.C,
        xpReward = xpReward,
        penaltyXp = penaltyXp,
        penaltyHp = penaltyHp,
        statRewards = buildMap {
            if (statStr > 0) put(com.arise.habitquest.domain.model.Stat.STR, statStr)
            if (statAgi > 0) put(com.arise.habitquest.domain.model.Stat.AGI, statAgi)
        },
        isCompleted = isCompleted,
        isFailed = isFailed,
        isSkipped = false,
        acceptedMiniVersion = false,
        dueDate = LocalDate.now(),
        scheduledTimeHint = null,
        streakCount = 1,
        isRecurring = true,
        parentTemplateId = null,
        progressCurrent = 0,
        progressTarget = 1,
        iconName = "task_alt",
        isSystemMandate = false
    )

    private class FakeMissionRepository(missions: List<Mission>) : MissionRepository {
        private val byId = missions.associateBy { it.id }.toMutableMap()

        override fun observeMissionsForDate(date: LocalDate): Flow<List<Mission>> =
            MutableStateFlow(byId.values.filter { it.dueDate == date })

        override fun observeWeeklyMissions(weekStart: LocalDate, weekEnd: LocalDate): Flow<List<Mission>> =
            MutableStateFlow(emptyList())

        override fun observeBossRaids(): Flow<List<Mission>> = MutableStateFlow(emptyList())
        override fun observePenaltyZone(): Flow<List<Mission>> = MutableStateFlow(emptyList())

        override suspend fun getMissionsForDate(date: LocalDate): List<Mission> =
            byId.values.filter { it.dueDate == date }

        override suspend fun getMissionById(id: String): Mission? = byId[id]

        override suspend fun deleteMissionById(id: String) {
            byId.remove(id)
        }

        override suspend fun insertMissions(missions: List<Mission>) {
            missions.forEach { byId[it.id] = it }
        }

        override suspend fun insertMission(mission: Mission) {
            byId[mission.id] = mission
        }

        override suspend fun deleteDailyMissionsForDate(date: LocalDate) = Unit
        override suspend fun failActiveDailyMissionsForDate(date: LocalDate) = Unit
        override suspend fun markCompleted(id: String, streak: Int, usedMini: Boolean) = Unit
        override suspend fun markFailed(id: String) = Unit
        override suspend fun markSkipped(id: String) = Unit

        override suspend fun resetOutcome(id: String) {
            val mission = byId[id] ?: return
            byId[id] = mission.copy(
                isCompleted = false,
                isFailed = false,
                isSkipped = false,
                acceptedMiniVersion = false,
                streakCount = 0
            )
        }

        override suspend fun pruneOldDailyMissions(cutoffDate: LocalDate) = Unit
        override suspend fun countCompletedForDate(date: LocalDate): Int =
            byId.values.count { it.dueDate == date && it.isCompleted }

        override suspend fun countDailyMissionsForDate(date: LocalDate): Int =
            byId.values.count { it.dueDate == date && it.type == MissionType.DAILY }

        override suspend fun getMissionsInRange(from: String, to: String): List<Mission> = emptyList()
    }

    private class FakeUserRepository(initial: UserProfile) : UserRepository {
        private val flow = MutableStateFlow(initial)
        var profile: UserProfile = initial
            private set

        override fun observeUserProfile(): Flow<UserProfile?> = flow

        override suspend fun getUserProfile(): UserProfile = profile

        override suspend fun upsertProfile(profile: UserProfile) {
            this.profile = profile
            flow.value = profile
        }

        override suspend fun updateXpAndLevel(xp: Long, level: Int, rank: Rank, xpToNext: Long) {
            profile = profile.copy(xp = xp, level = level, rank = rank, xpToNextLevel = xpToNext)
            flow.value = profile
        }

        override suspend fun updateHp(hp: Int) {
            profile = profile.copy(hp = hp)
            flow.value = profile
        }

        override suspend fun updateStreak(current: Int, best: Int) {
            profile = profile.copy(streakCurrent = current, streakBest = best)
            flow.value = profile
        }

        override suspend fun updateStats(stats: HunterStats) {
            profile = profile.copy(stats = stats)
            flow.value = profile
        }

        override suspend fun incrementMissionStats(xpGained: Long) {
            profile = profile.copy(
                totalMissionsCompleted = profile.totalMissionsCompleted + 1,
                totalXpEarned = profile.totalXpEarned + xpGained
            )
            flow.value = profile
        }

        override suspend fun decrementMissionStats(completedDelta: Int, xpDelta: Long) {
            profile = profile.copy(
                totalMissionsCompleted = (profile.totalMissionsCompleted - completedDelta).coerceAtLeast(0),
                totalXpEarned = (profile.totalXpEarned - xpDelta).coerceAtLeast(0)
            )
            flow.value = profile
        }

        override suspend fun updateShields(shields: Int) = Unit
        override suspend fun updateAdaptiveDifficulty(difficulty: Float) = Unit
        override suspend fun incrementDayCount() = Unit

        override suspend fun updateMissState(consecutiveMissDays: Int, pendingWarning: Boolean) {
            profile = profile.copy(consecutiveMissDays = consecutiveMissDays, pendingWarning = pendingWarning)
            flow.value = profile
        }

        override suspend fun getShadowCompletions(templateIds: List<String>): Map<String, Int> = emptyMap()
    }
}
