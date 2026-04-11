package com.arise.habitquest.data.repository

import com.arise.habitquest.data.local.database.dao.AchievementDao
import com.arise.habitquest.data.mapper.AchievementMapper
import com.arise.habitquest.domain.model.Achievement
import com.arise.habitquest.domain.model.AchievementTrigger
import com.arise.habitquest.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class AchievementRepositoryImpl @Inject constructor(
    private val dao: AchievementDao,
    private val mapper: AchievementMapper
) : AchievementRepository {

    override fun observeAllAchievements(): Flow<List<Achievement>> =
        dao.observeAllAchievements().map { list -> list.map(mapper::toDomain) }

    override suspend fun getUnlockedAchievements(): List<Achievement> =
        dao.getUnlockedAchievements().map(mapper::toDomain)

    override suspend fun getAchievementsByTrigger(trigger: AchievementTrigger): List<Achievement> =
        dao.getAchievementsByTrigger(trigger.name).map(mapper::toDomain)

    override suspend fun insertAchievements(achievements: List<Achievement>) =
        dao.insertAchievements(achievements.map(mapper::toEntity))

    override suspend fun unlockAchievement(id: String) =
        dao.unlockAchievement(id, Instant.now().toEpochMilli())

    override suspend fun updateProgress(id: String, progress: Int) =
        dao.updateProgress(id, progress)

    override suspend fun countUnlocked(): Int = dao.countUnlocked()
}
