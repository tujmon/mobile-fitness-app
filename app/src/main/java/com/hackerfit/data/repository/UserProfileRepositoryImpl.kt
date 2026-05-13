package com.hackerfit.data.repository

import com.hackerfit.data.local.db.dao.AssessmentLogDao
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
    private val dao: UserProfileDao,
    private val assessmentLogDao: AssessmentLogDao
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
        require(rungNumber in 1..48) { "Degrau invalido: $rungNumber" }
        val phase = if (rungNumber <= 15) "introductory" else "lifetime"
        dao.updateRung(rungNumber, phase, LocalDate.now())
    }

    override suspend fun completeOnboarding() {
        if (dao.completeOnboarding() == 0) {
            ensureProfileExists()
            dao.completeOnboarding()
        }
    }

    override suspend fun setReminderTime(hour: Int, minute: Int) {
        require(hour in 0..23) { "Hora invalida: $hour" }
        require(minute in 0..59) { "Minuto invalido: $minute" }
        if (dao.setReminderTime(hour, minute) == 0) {
            ensureProfileExists()
            dao.setReminderTime(hour, minute)
        }
    }

    override suspend fun clearReminderTime() {
        if (dao.clearReminderTime() == 0) {
            ensureProfileExists()
            dao.clearReminderTime()
        }
    }

    private suspend fun ensureProfileExists() {
        if (dao.getProfileOnce() == null) {
            dao.saveProfile(UserProfileEntity())
        }
    }

    override suspend fun recalculateCurrentRung() {
        val allAssessments = assessmentLogDao.getAllAssessmentsList().sortedBy { it.date }
        var rung = 1
        var rungDate: LocalDate? = null
        for (assessment in allAssessments) {
            if (assessment.passed && assessment.fromRung == rung && assessment.toRung == rung + 1) {
                rung = assessment.toRung
                rungDate = assessment.date
            }
        }
        val phase = if (rung <= 15) "introductory" else "lifetime"
        val startDate = rungDate ?: dao.getProfileOnce()?.rungStartDate ?: LocalDate.now()
        dao.updateRung(rung, phase, startDate)
    }
}
