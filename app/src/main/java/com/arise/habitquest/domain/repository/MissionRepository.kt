package com.arise.habitquest.domain.repository

import com.arise.habitquest.domain.model.Mission
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MissionRepository {
    fun observeMissionsForDate(date: LocalDate): Flow<List<Mission>>
    fun observeWeeklyMissions(weekStart: LocalDate, weekEnd: LocalDate): Flow<List<Mission>>
    fun observeBossRaids(): Flow<List<Mission>>
    fun observePenaltyZone(): Flow<List<Mission>>
    suspend fun getMissionsForDate(date: LocalDate): List<Mission>
    suspend fun getMissionById(id: String): Mission?
    suspend fun deleteMissionById(id: String)
    suspend fun insertMissions(missions: List<Mission>)
    suspend fun insertMission(mission: Mission)
    suspend fun deleteDailyMissionsForDate(date: LocalDate)
    suspend fun failActiveDailyMissionsForDate(date: LocalDate)
    suspend fun markCompleted(id: String, streak: Int, usedMini: Boolean)
    suspend fun markFailed(id: String)
    suspend fun markSkipped(id: String)
    suspend fun resetOutcome(id: String)
    suspend fun pruneOldDailyMissions(cutoffDate: LocalDate)
    suspend fun countCompletedForDate(date: LocalDate): Int
    suspend fun countDailyMissionsForDate(date: LocalDate): Int
    suspend fun getMissionsInRange(from: String, to: String): List<Mission>
}
