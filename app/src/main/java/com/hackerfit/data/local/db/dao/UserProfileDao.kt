package com.hackerfit.data.local.db.dao

import androidx.room.*
import com.hackerfit.data.local.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET currentRung = :rung, phase = :phase, rungStartDate = :date WHERE id = 1")
    suspend fun updateRung(rung: Int, phase: String, date: java.time.LocalDate)

    @Query("UPDATE user_profile SET onboardingComplete = 1 WHERE id = 1")
    suspend fun completeOnboarding(): Int

    @Query("UPDATE user_profile SET dailyReminderHour = :hour, dailyReminderMinute = :minute WHERE id = 1")
    suspend fun setReminderTime(hour: Int, minute: Int): Int

    @Query("UPDATE user_profile SET dailyReminderHour = NULL, dailyReminderMinute = NULL WHERE id = 1")
    suspend fun clearReminderTime(): Int

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileOnce(): UserProfileEntity?
}
