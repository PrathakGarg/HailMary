package com.arise.habitquest.widget

import android.content.Context
import com.arise.habitquest.data.local.database.AppDatabase
import com.arise.habitquest.data.time.TimeProvider

data class WidgetData(
    val hunterName: String,
    val rank: String,
    val hp: Int,
    val maxHp: Int,
    val hpFraction: Float,
    val xpFraction: Float,
    val streak: Int,
    val completedToday: Int,
    val totalToday: Int
)

object WidgetDataProvider {

    suspend fun load(context: Context): WidgetData {
        val db = AppDatabase.getInstance(context)
        val profile = db.userProfileDao().getUserProfile()
        val todayMissions = try {
            val today = TimeProvider.getInstance(context).sessionDay().toString()
            db.missionDao().getMissionsForDate(today)
        } catch (_: Exception) { emptyList() }

        val hp = profile?.hp ?: 100
        val maxHp = profile?.maxHp ?: 100
        val xp = profile?.xp ?: 0L
        val xpToNext = profile?.xpToNextLevel ?: 100L

        return WidgetData(
            hunterName = profile?.hunterName ?: "HUNTER",
            rank = profile?.rank ?: "E",
            hp = hp,
            maxHp = maxHp,
            hpFraction = if (maxHp > 0) hp.toFloat() / maxHp else 0f,
            xpFraction = if (xpToNext > 0) xp.toFloat() / xpToNext else 0f,
            streak = profile?.streakCurrent ?: 0,
            completedToday = todayMissions.count { it.isCompleted },
            totalToday = todayMissions.size
        )
    }
}
