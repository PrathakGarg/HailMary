package com.arise.habitquest.data.repository

import com.arise.habitquest.data.local.database.dao.ShadowDao
import com.arise.habitquest.data.local.database.dao.UserProfileDao
import com.arise.habitquest.data.mapper.UserProfileMapper
import com.arise.habitquest.domain.model.HunterStats
import com.arise.habitquest.domain.model.Rank
import com.arise.habitquest.domain.model.UserProfile
import com.arise.habitquest.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao,
    private val shadowDao: ShadowDao,
    private val mapper: UserProfileMapper
) : UserRepository {

    override fun observeUserProfile(): Flow<UserProfile?> =
        dao.observeUserProfile().map { it?.let(mapper::toDomain) }

    override suspend fun getUserProfile(): UserProfile? =
        dao.getUserProfile()?.let(mapper::toDomain)

    override suspend fun upsertProfile(profile: UserProfile) =
        dao.upsertProfile(mapper.toEntity(profile))

    override suspend fun updateXpAndLevel(xp: Long, level: Int, rank: Rank, xpToNext: Long) =
        dao.updateXpAndLevel(xp, level, rank.name, xpToNext)

    override suspend fun updateHp(hp: Int) = dao.updateHp(hp)

    override suspend fun updateStreak(current: Int, best: Int) = dao.updateStreak(current, best)

    override suspend fun updateStats(stats: HunterStats) =
        dao.updateStats(stats.str, stats.agi, stats.int, stats.vit, stats.end, stats.sense)

    override suspend fun incrementMissionStats(xpGained: Long) =
        dao.incrementMissionStats(xpGained)

    override suspend fun updateShields(shields: Int) = dao.updateShields(shields)

    override suspend fun updateAdaptiveDifficulty(difficulty: Float) =
        dao.updateAdaptiveDifficulty(difficulty)

    override suspend fun incrementDayCount() = dao.incrementDayCount()

    override suspend fun updateMissState(consecutiveMissDays: Int, pendingWarning: Boolean) =
        dao.updateMissState(consecutiveMissDays, pendingWarning)

    override suspend fun getShadowCompletions(templateIds: List<String>): Map<String, Int> {
        return templateIds.mapNotNull { id ->
            shadowDao.getShadow(id)?.let { id to it.totalCompletions }
        }.toMap()
    }
}
