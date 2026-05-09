package com.hackerfit.domain.repository

import com.hackerfit.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getProfile(): Flow<UserProfile?>
    suspend fun saveProfile(profile: UserProfile)
    suspend fun updateRung(rungNumber: Int)
    suspend fun completeOnboarding()
    suspend fun setReminderTime(hour: Int, minute: Int)
    suspend fun clearReminderTime()
}
