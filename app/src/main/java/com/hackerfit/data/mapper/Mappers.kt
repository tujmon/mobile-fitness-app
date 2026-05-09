package com.hackerfit.data.mapper

import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import com.hackerfit.data.local.db.entity.DailyLogEntity
import com.hackerfit.data.local.db.entity.UserProfileEntity
import com.hackerfit.domain.model.*

fun UserProfileEntity.toDomain() = UserProfile(
    currentRung = currentRung,
    phase = if (phase == "introductory") Phase.INTRODUCTORY else Phase.LIFETIME,
    rungStartDate = rungStartDate,
    dailyReminderHour = dailyReminderHour,
    dailyReminderMinute = dailyReminderMinute,
    onboardingComplete = onboardingComplete
)

fun DailyLogEntity.toDomain() = DailyLog(
    id = id,
    date = date,
    rung = rung,
    completed = completed,
    completedAt = completedAt
)

fun AssessmentLogEntity.toDomain() = AssessmentLog(
    id = id,
    date = date,
    fromRung = fromRung,
    toRung = toRung,
    passed = passed,
    notes = notes
)
