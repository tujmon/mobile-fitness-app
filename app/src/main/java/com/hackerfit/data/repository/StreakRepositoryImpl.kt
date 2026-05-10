package com.hackerfit.data.repository

import com.hackerfit.data.local.preferences.StreakDataStore
import com.hackerfit.domain.model.StreakData
import com.hackerfit.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepositoryImpl @Inject constructor(
    private val dataStore: StreakDataStore
) : StreakRepository {

    companion object {
        private const val MAX_FREEZES = 5
        private const val DAYS_PER_FREEZE = 5
    }

    override fun getStreakData(): Flow<StreakData> = dataStore.streakData

    override suspend fun incrementStreak() {
        val current = dataStore.streakData.first()
        val newCount = current.streakCount + 1
        val newFreezes = if (newCount > 0 && newCount % DAYS_PER_FREEZE == 0 && current.freezesBanked < MAX_FREEZES) {
            current.freezesBanked + 1
        } else {
            current.freezesBanked
        }
        dataStore.updateStreakData(
            current.copy(
                streakCount = newCount,
                freezesBanked = newFreezes,
                lastFreezeEarnDate = if (newFreezes > current.freezesBanked) LocalDate.now() else current.lastFreezeEarnDate
            )
        )
    }

    override suspend fun resetStreak() {
        val current = dataStore.streakData.first()
        dataStore.updateStreakData(current.copy(streakCount = 0))
    }

    override suspend fun useFreeze() {
        val current = dataStore.streakData.first()
        if (current.freezesBanked > 0) {
            dataStore.updateStreakData(
                current.copy(freezesBanked = current.freezesBanked - 1)
            )
        }
    }

    override suspend fun recalculateStreak() {}
}
