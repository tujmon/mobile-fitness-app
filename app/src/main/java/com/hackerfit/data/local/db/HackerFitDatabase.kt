package com.hackerfit.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hackerfit.data.local.db.dao.AssessmentLogDao
import com.hackerfit.data.local.db.dao.DailyLogDao
import com.hackerfit.data.local.db.dao.UserProfileDao
import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import com.hackerfit.data.local.db.entity.DailyLogEntity
import com.hackerfit.data.local.db.entity.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        DailyLogEntity::class,
        AssessmentLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HackerFitDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun assessmentLogDao(): AssessmentLogDao
}
