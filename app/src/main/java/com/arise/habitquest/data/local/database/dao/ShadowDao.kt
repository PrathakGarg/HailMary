package com.arise.habitquest.data.local.database.dao

import androidx.room.*
import com.arise.habitquest.data.local.database.entity.ShadowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShadowDao {

    @Query("SELECT * FROM shadows ORDER BY is_shadow DESC, current_streak DESC")
    fun observeAllShadows(): Flow<List<ShadowEntity>>

    @Query("SELECT * FROM shadows WHERE is_shadow = 1")
    fun observeShadowArmy(): Flow<List<ShadowEntity>>

    @Query("SELECT * FROM shadows WHERE template_id = :id LIMIT 1")
    suspend fun getShadow(id: String): ShadowEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertShadow(shadow: ShadowEntity)

    @Update
    suspend fun updateShadow(shadow: ShadowEntity)

    @Query("UPDATE shadows SET current_streak = :streak, best_streak = :best, total_completions = total_completions + 1, last_completed_date = :date WHERE template_id = :id")
    suspend fun recordCompletion(id: String, streak: Int, best: Int, date: String)

    @Query("UPDATE shadows SET is_shadow = 1 WHERE template_id = :id")
    suspend fun promotToShadow(id: String)

    @Query("UPDATE shadows SET current_streak = 0 WHERE template_id = :id")
    suspend fun resetStreak(id: String)
}
