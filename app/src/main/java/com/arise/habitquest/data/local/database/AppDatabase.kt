package com.arise.habitquest.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arise.habitquest.data.local.database.dao.*
import com.arise.habitquest.data.local.database.entity.*

@Database(
    entities = [
        UserProfileEntity::class,
        MissionEntity::class,
        AchievementEntity::class,
        DailyLogEntity::class,
        ShadowEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun missionDao(): MissionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun shadowDao(): ShadowDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // v1 → v2: added is_system_mandate column to missions table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE missions ADD COLUMN is_system_mandate INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arise_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
    }
}
