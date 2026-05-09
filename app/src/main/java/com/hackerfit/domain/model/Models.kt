package com.hackerfit.domain.model

import java.time.LocalDate

data class UserProfile(
    val currentRung: Int,
    val phase: Phase,
    val rungStartDate: LocalDate,
    val dailyReminderHour: Int?,
    val dailyReminderMinute: Int?,
    val onboardingComplete: Boolean
)

data class DailyLog(
    val id: Long = 0,
    val date: LocalDate,
    val rung: Int,
    val completed: Boolean,
    val completedAt: LocalDate? = null
)

data class AssessmentLog(
    val id: Long = 0,
    val date: LocalDate,
    val fromRung: Int,
    val toRung: Int,
    val passed: Boolean,
    val notes: String? = null
)

data class StreakData(
    val streakCount: Int,
    val freezesBanked: Int,
    val lastFreezeEarnDate: LocalDate?
)
