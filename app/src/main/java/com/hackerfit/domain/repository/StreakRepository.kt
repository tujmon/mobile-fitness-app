package com.hackerfit.domain.repository

import com.hackerfit.domain.model.StreakData
import kotlinx.coroutines.flow.Flow

interface StreakRepository {
    fun getStreakData(): Flow<StreakData>
    suspend fun incrementStreak()
    suspend fun resetStreak()
    suspend fun useFreeze()
    suspend fun recalculateStreak()
}
