package com.hackerfit

import com.hackerfit.domain.model.*
import com.hackerfit.domain.repository.AssessmentRepository
import com.hackerfit.domain.repository.DailyLogRepository
import com.hackerfit.domain.repository.StreakRepository
import com.hackerfit.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class FakeUserProfileRepository : UserProfileRepository {

    val profileState = MutableStateFlow<UserProfile?>(null)

    fun setProfile(profile: UserProfile?) {
        profileState.value = profile
    }

    override fun getProfile(): Flow<UserProfile?> = profileState

    override suspend fun saveProfile(profile: UserProfile) {
        profileState.value = profile
    }

    override suspend fun updateRung(rungNumber: Int) {
        require(rungNumber in 1..48) { "Degrau invalido: $rungNumber" }
        profileState.value?.let {
            val phase = if (rungNumber <= 15) Phase.INTRODUCTORY else Phase.LIFETIME
            profileState.value = it.copy(currentRung = rungNumber, phase = phase)
        }
    }

    override suspend fun completeOnboarding() {
        profileState.value?.let {
            profileState.value = it.copy(onboardingComplete = true)
        }
    }

    override suspend fun setReminderTime(hour: Int, minute: Int) {
        require(hour in 0..23) { "Hora invalida: $hour" }
        require(minute in 0..59) { "Minuto invalido: $minute" }
        profileState.value?.let {
            profileState.value = it.copy(dailyReminderHour = hour, dailyReminderMinute = minute)
        }
    }

    override suspend fun clearReminderTime() {
        profileState.value?.let {
            profileState.value = it.copy(dailyReminderHour = null, dailyReminderMinute = null)
        }
    }

    override suspend fun recalculateCurrentRung() {}
}

class FakeDailyLogRepository : DailyLogRepository {

    val logsState = MutableStateFlow<List<DailyLog>>(emptyList())

    fun setLogs(logs: List<DailyLog>) {
        logsState.value = logs
    }

    override fun getAllLogs(): Flow<List<DailyLog>> = logsState

    override fun getLogsInRange(start: LocalDate, end: LocalDate): Flow<List<DailyLog>> =
        logsState.map { logs -> logs.filter { it.date in start..end } }

    override suspend fun getLogForDate(date: LocalDate): DailyLog? =
        logsState.value.find { it.date == date }

    override suspend fun saveLog(log: DailyLog) {
        val current = logsState.value.toMutableList()
        val idx = current.indexOfFirst { it.date == log.date }
        if (idx >= 0) current[idx] = log else current.add(log)
        logsState.value = current
    }

    override suspend fun hasCompletedToday(): Boolean =
        logsState.value.any { it.date == LocalDate.now() && it.completed }

    override fun observeCompletedToday(): Flow<Boolean> =
        logsState.map { logs -> logs.any { it.date == LocalDate.now() && it.completed } }

    override suspend fun getConsecutiveDays(): Int {
        var count = 0
        var date = LocalDate.now()
        while (logsState.value.any { it.date == date && it.completed }) {
            count++
            date = date.minusDays(1)
        }
        return count
    }

    override suspend fun deleteLog(id: Long) {
        logsState.value = logsState.value.filter { it.id != id }
    }
}

class FakeStreakRepository : StreakRepository {

    val streakState = MutableStateFlow(StreakData(0, 0, null))

    fun setStreakData(data: StreakData) {
        streakState.value = data
    }

    override fun getStreakData(): Flow<StreakData> = streakState

    override suspend fun incrementStreak() {
        val current = streakState.value
        val newCount = current.streakCount + 1
        val newFreezes = if (newCount > 0 && newCount % 5 == 0 && current.freezesBanked < 5) {
            current.freezesBanked + 1
        } else {
            current.freezesBanked
        }
        streakState.value = current.copy(
            streakCount = newCount,
            freezesBanked = newFreezes,
            lastFreezeEarnDate = if (newFreezes > current.freezesBanked) LocalDate.now() else current.lastFreezeEarnDate
        )
    }

    override suspend fun resetStreak() {
        streakState.value = streakState.value.copy(streakCount = 0)
    }

    override suspend fun useFreeze() {
        val current = streakState.value
        if (current.freezesBanked > 0) {
            streakState.value = current.copy(freezesBanked = current.freezesBanked - 1)
        }
    }

    override suspend fun recalculateStreak() {}
}

class FakeAssessmentRepository : AssessmentRepository {

    val assessmentsState = MutableStateFlow<List<AssessmentLog>>(emptyList())

    fun setAssessments(assessments: List<AssessmentLog>) {
        assessmentsState.value = assessments
    }

    override fun getAllAssessments(): Flow<List<AssessmentLog>> = assessmentsState

    override suspend fun saveAssessment(assessment: AssessmentLog) {
        assessmentsState.value = assessmentsState.value + assessment
    }

    override suspend fun getLastAssessmentDate(): LocalDate? =
        assessmentsState.value.maxOfOrNull { it.date }

    override suspend fun deleteAssessment(id: Long) {
        assessmentsState.value = assessmentsState.value.filter { it.id != id }
    }
}
