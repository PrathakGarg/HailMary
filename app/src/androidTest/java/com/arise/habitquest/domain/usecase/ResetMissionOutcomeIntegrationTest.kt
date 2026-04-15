package com.arise.habitquest.domain.usecase

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arise.habitquest.data.local.database.AppDatabase
import com.arise.habitquest.data.local.database.entity.MissionEntity
import com.arise.habitquest.data.local.database.entity.UserProfileEntity
import com.arise.habitquest.data.local.datastore.OnboardingDataStore
import com.arise.habitquest.data.mapper.MissionMapper
import com.arise.habitquest.data.mapper.UserProfileMapper
import com.arise.habitquest.data.repository.MissionRepositoryImpl
import com.arise.habitquest.data.repository.UserRepositoryImpl
import com.arise.habitquest.data.time.TimeProvider
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResetMissionOutcomeIntegrationTest {

    @Test
    fun resetCompletedMission_withRealDb_reopensMission() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getInstance(context)
        val dataStore = OnboardingDataStore(context)

        db.clearAllTables()
        dataStore.clearMissionRollbackLedger()

        val sessionDate = TimeProvider.getInstance(context).sessionDay().toString()

        db.userProfileDao().upsertProfile(
            UserProfileEntity(
                hunterName = "Integration Hunter",
                rank = "E",
                level = 1,
                xp = 20,
                xpToNextLevel = 100,
                hp = 100,
                maxHp = 100,
                onboardingComplete = true,
                joinDate = sessionDate
            )
        )

        val missionId = "integration_reset_1"
        db.missionDao().insertMission(
            MissionEntity(
                id = missionId,
                title = "Integration Completed Mission",
                description = "desc",
                systemLore = "lore",
                type = "DAILY",
                category = "PRODUCTIVITY",
                difficulty = "E",
                xpReward = 10,
                penaltyXp = 5,
                penaltyHp = 5,
                statRewardsJson = JSONObject(mapOf("STR" to 1)).toString(),
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
                dueDate = sessionDate,
                scheduledTimeHint = "MORNING",
                iconName = "task_alt"
            )
        )

        val missionRepository = MissionRepositoryImpl(db.missionDao(), MissionMapper(), TimeProvider.getInstance(context), context)
        val userRepository = UserRepositoryImpl(db.userProfileDao(), db.shadowDao(), UserProfileMapper())
        val useCase = ResetMissionOutcomeUseCase(missionRepository, userRepository, dataStore)

        val reset = useCase(missionId)
        assertTrue(reset)

        val missionAfter = db.missionDao().getMissionById(missionId)
        assertTrue(missionAfter != null)
        assertFalse(missionAfter!!.isCompleted)
        assertFalse(missionAfter.isFailed)
        assertFalse(missionAfter.isSkipped)
    }
}
