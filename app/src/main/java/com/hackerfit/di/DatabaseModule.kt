package com.hackerfit.di

import android.content.Context
import androidx.room.Room
import com.hackerfit.data.local.db.HackerFitDatabase
import com.hackerfit.data.local.db.MIGRATION_1_2
import com.hackerfit.data.local.db.dao.AssessmentLogDao
import com.hackerfit.data.local.db.dao.DailyLogDao
import com.hackerfit.data.local.db.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HackerFitDatabase {
        return Room.databaseBuilder(
            context,
            HackerFitDatabase::class.java,
            "hackerfit.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideUserProfileDao(db: HackerFitDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideDailyLogDao(db: HackerFitDatabase): DailyLogDao = db.dailyLogDao()

    @Provides
    fun provideAssessmentLogDao(db: HackerFitDatabase): AssessmentLogDao = db.assessmentLogDao()
}
