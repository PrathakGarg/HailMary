package com.arise.habitquest.data.repository

import com.arise.habitquest.data.local.database.dao.MissionDao
import com.arise.habitquest.data.mapper.MissionMapper
import com.arise.habitquest.data.time.TimeProvider
import com.arise.habitquest.domain.model.Mission
import com.arise.habitquest.domain.repository.MissionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class MissionRepositoryImpl @Inject constructor(
    private val dao: MissionDao,
    private val mapper: MissionMapper,
    private val timeProvider: TimeProvider
) : MissionRepository {

    override fun observeMissionsForDate(date: LocalDate): Flow<List<Mission>> =
        dao.observeMissionsForDate(date.toString()).map { list -> list.map(mapper::toDomain) }

    override fun observeWeeklyMissions(weekStart: LocalDate, weekEnd: LocalDate): Flow<List<Mission>> =
        dao.observeWeeklyMissions(weekStart.toString(), weekEnd.toString())
            .map { list -> list.map(mapper::toDomain) }

    override fun observeBossRaids(): Flow<List<Mission>> =
        dao.observeBossRaids().map { list -> list.map(mapper::toDomain) }

    override fun observePenaltyZone(): Flow<List<Mission>> =
        dao.observePenaltyZone().map { list -> list.map(mapper::toDomain) }

    override suspend fun getMissionsForDate(date: LocalDate): List<Mission> =
        dao.getMissionsForDate(date.toString()).map(mapper::toDomain)

    override suspend fun getMissionById(id: String): Mission? =
        dao.getMissionById(id)?.let(mapper::toDomain)

    override suspend fun insertMissions(missions: List<Mission>) =
        dao.insertMissions(missions.map(mapper::toEntity))

    override suspend fun insertMission(mission: Mission) =
        dao.insertMission(mapper.toEntity(mission))

    override suspend fun deleteDailyMissionsForDate(date: LocalDate) =
        dao.deleteDailyMissionsForDate(date.toString())

    override suspend fun markCompleted(id: String, streak: Int, usedMini: Boolean) =
        dao.markCompleted(id, timeProvider.nowMillis(), streak, usedMini)

    override suspend fun markFailed(id: String) = dao.markFailed(id)

    override suspend fun markSkipped(id: String) = dao.markSkipped(id)

    override suspend fun pruneOldDailyMissions(cutoffDate: LocalDate) =
        dao.pruneOldDailyMissions(cutoffDate.toString())

    override suspend fun countCompletedForDate(date: LocalDate): Int =
        dao.countCompletedForDate(date.toString())

    override suspend fun countDailyMissionsForDate(date: LocalDate): Int =
        dao.countDailyMissionsForDate(date.toString())

    override suspend fun getMissionsInRange(from: String, to: String): List<Mission> =
        dao.getMissionsInRange(from, to).map(mapper::toDomain)
}
