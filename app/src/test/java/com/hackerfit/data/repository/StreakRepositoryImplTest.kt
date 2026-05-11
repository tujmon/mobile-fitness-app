package com.hackerfit.data.repository

import com.hackerfit.data.local.preferences.StreakDataStore
import com.hackerfit.domain.model.StreakData
import com.hackerfit.domain.repository.DailyLogRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class StreakRepositoryImplTest {

    private lateinit var dataStore: StreakDataStore
    private lateinit var dailyLogRepository: DailyLogRepository
    private lateinit var repository: StreakRepositoryImpl

    @Before
    fun setup() {
        dataStore = mockk(relaxed = true)
        dailyLogRepository = mockk(relaxed = true)
        every { dataStore.streakData } returns flowOf(StreakData(5, 1, LocalDate.now()))
        repository = StreakRepositoryImpl(dataStore, dailyLogRepository)
    }

    @Test
    fun `getStreakData returns flow from dataStore`() = runTest {
        val result = repository.getStreakData().first()
        assertEquals(5, result.streakCount)
        assertEquals(1, result.freezesBanked)
    }

    @Test
    fun `getStreakData returns default when empty`() = runTest {
        every { dataStore.streakData } returns flowOf(StreakData(0, 0, null))
        val result = repository.getStreakData().first()
        assertEquals(0, result.streakCount)
        assertEquals(0, result.freezesBanked)
        assertNull(result.lastFreezeEarnDate)
    }

    @Test
    fun `getStreakData returns data with freezes`() = runTest {
        val date = LocalDate.of(2025, 3, 1)
        every { dataStore.streakData } returns flowOf(StreakData(10, 2, date))
        val result = repository.getStreakData().first()
        assertEquals(10, result.streakCount)
        assertEquals(2, result.freezesBanked)
        assertEquals(date, result.lastFreezeEarnDate)
    }

    @Test
    fun `incrementStreak calls updateStreakData`() = runTest {
        repository.incrementStreak()
        coVerify { dataStore.updateStreakData(any()) }
    }

    @Test
    fun `resetStreak calls updateStreakData`() = runTest {
        repository.resetStreak()
        coVerify { dataStore.updateStreakData(any()) }
    }

    @Test
    fun `useFreeze calls updateStreakData`() = runTest {
        repository.useFreeze()
        coVerify { dataStore.updateStreakData(any()) }
    }

    @Test
    fun `recalculateStreak does nothing`() = runTest {
        repository.recalculateStreak()
    }
}
