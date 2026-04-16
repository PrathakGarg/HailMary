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
        ShadowEntity::class,
        MissionTrackingLogEntity::class
    ],
        version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun missionDao(): MissionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun shadowDao(): ShadowDao
    abstract fun missionTrackingDao(): MissionTrackingDao

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

        // v2 → v3: add physical_family + muscle_load_json to missions table
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE missions ADD COLUMN physical_family TEXT NOT NULL DEFAULT 'UNSPECIFIED'"
                )
                db.execSQL(
                    "ALTER TABLE missions ADD COLUMN muscle_load_json TEXT NOT NULL DEFAULT '{}'"
                )
            }
        }

            // v3 → v4: add progression profile/state columns to user_profile
            val MIGRATION_3_4 = object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE user_profile ADD COLUMN track_focus TEXT NOT NULL DEFAULT 'PHYSICAL'")
                    db.execSQL("ALTER TABLE user_profile ADD COLUMN equipment_mode TEXT NOT NULL DEFAULT 'BODYWEIGHT'")
                    db.execSQL("ALTER TABLE user_profile ADD COLUMN schedule_style TEXT NOT NULL DEFAULT 'FIXED_WINDOW'")
                    db.execSQL("ALTER TABLE user_profile ADD COLUMN shoulder_risk_flag INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE user_profile ADD COLUMN heat_risk_flag INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("ALTER TABLE user_profile ADD COLUMN progression_preference TEXT NOT NULL DEFAULT 'ASSERTIVE_SAFE'")
                    db.execSQL("ALTER TABLE user_profile ADD COLUMN progression_state TEXT NOT NULL DEFAULT 'PROGRESSING'")
                    db.execSQL("ALTER TABLE user_profile ADD COLUMN transition_recommendation TEXT NOT NULL DEFAULT ''")
                }
            }

        // v4 -> v5: add mission_tracking_logs table for structured mission metrics
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS mission_tracking_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        mission_id TEXT NOT NULL,
                        mission_title TEXT NOT NULL,
                        mission_due_date TEXT NOT NULL,
                        primary_label TEXT NOT NULL,
                        primary_value TEXT NOT NULL,
                        secondary_label TEXT NOT NULL,
                        secondary_value TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_mission_tracking_logs_mission_id ON mission_tracking_logs(mission_id)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_mission_tracking_logs_created_at ON mission_tracking_logs(created_at)"
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
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build().also { INSTANCE = it }
            }
    }
}
