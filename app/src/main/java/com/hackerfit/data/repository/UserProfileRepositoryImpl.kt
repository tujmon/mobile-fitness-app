package com.hackerfit.data.repository

import com.hackerfit.data.local.db.dao.UserProfileDao
import com.hackerfit.data.local.db.entity.UserProfileEntity
import com.hackerfit.data.mapper.toDomain
import com.hackerfit.domain.model.Phase
import com.hackerfit.domain.model.UserProfile
import com.hackerfit.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val dao: UserProfileDao
) : UserProfileRepository {

    override fun getProfile(): Flow<UserProfile?> {
        return dao.getProfile().map { it?.toDomain() }
    }

    override suspend fun saveProfile(profile: UserProfile) {
        dao.saveProfile(
            UserProfileEntity(
                currentRung = profile.currentRung,
                phase = if (profile.phase == Phase.INTRODUCTORY) "introductory" else "lifetime",
                rungStartDate = profile.rungStartDate,
                dailyReminderHour = profile.dailyReminderHour,
                dailyReminderMinute = profile.dailyReminderMinute,
                onboardingComplete = profile.onboardingComplete
            )
        )
    }

    override suspend fun updateRung(rungNumber: Int) {
        val phase = if (rungNumber <= 15) "introductory" else "lifetime"
        dao.updateRung(rungNumber, phase, LocalDate.now())
    }

    override suspend fun completeOnboarding() {
        dao.completeOnboarding()
    }

    override suspend fun setReminderTime(hour: Int, minute: Int) {
        dao.setReminderTime(hour, minute)
    }

    override suspend fun clearReminderTime() {
        dao.clearReminderTime()
    }
}
