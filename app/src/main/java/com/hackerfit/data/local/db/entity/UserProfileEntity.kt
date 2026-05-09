package com.hackerfit.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val currentRung: Int = 1,
    val phase: String = "introductory",
    val rungStartDate: LocalDate = LocalDate.now(),
    val dailyReminderHour: Int? = 8,
    val dailyReminderMinute: Int? = 0,
    val onboardingComplete: Boolean = false
)
