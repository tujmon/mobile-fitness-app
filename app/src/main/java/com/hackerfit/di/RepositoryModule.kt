package com.hackerfit.di

import com.hackerfit.data.repository.AssessmentRepositoryImpl
import com.hackerfit.data.repository.DailyLogRepositoryImpl
import com.hackerfit.data.repository.StreakRepositoryImpl
import com.hackerfit.data.repository.UserProfileRepositoryImpl
import com.hackerfit.domain.repository.AssessmentRepository
import com.hackerfit.domain.repository.DailyLogRepository
import com.hackerfit.domain.repository.StreakRepository
import com.hackerfit.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindDailyLogRepository(impl: DailyLogRepositoryImpl): DailyLogRepository

    @Binds
    @Singleton
    abstract fun bindAssessmentRepository(impl: AssessmentRepositoryImpl): AssessmentRepository

    @Binds
    @Singleton
    abstract fun bindStreakRepository(impl: StreakRepositoryImpl): StreakRepository
}
