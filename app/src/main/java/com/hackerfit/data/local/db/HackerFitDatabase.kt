package com.hackerfit.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hackerfit.data.local.db.dao.AssessmentLogDao
import com.hackerfit.data.local.db.dao.DailyLogDao
import com.hackerfit.data.local.db.dao.UserProfileDao
import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import com.hackerfit.data.local.db.entity.DailyLogEntity
import com.hackerfit.data.local.db.entity.UserProfileEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_log_date` ON `daily_log` (`date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_log_completed_date` ON `daily_log` (`completed`, `date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_assessment_log_date` ON `assessment_log` (`date`)")
    }
}

@Database(
    entities = [
        UserProfileEntity::class,
        DailyLogEntity::class,
        AssessmentLogEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class HackerFitDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun assessmentLogDao(): AssessmentLogDao
}
