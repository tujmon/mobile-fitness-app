package com.hackerfit.data.local.preferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hackerfit.domain.model.StreakData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class StreakDataStoreTest {

    private lateinit var dataStore: StreakDataStore

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataStore = StreakDataStore(context)
    }

    @Test
    fun defaultStreakDataIsZero() = runTest {
        val data = dataStore.streakData.first()
        assertEquals(0, data.streakCount)
        assertEquals(0, data.freezesBanked)
        assertNull(data.lastFreezeEarnDate)
    }

    @Test
    fun updateStreakDataSavesCorrectly() = runTest {
        val date = LocalDate.of(2025, 3, 15)
        dataStore.updateStreakData(StreakData(10, 2, date))
        val data = dataStore.streakData.first()
        assertEquals(10, data.streakCount)
        assertEquals(2, data.freezesBanked)
        assertEquals(date, data.lastFreezeEarnDate)
    }

    @Test
    fun updateStreakDataOverwritesPrevious() = runTest {
        dataStore.updateStreakData(StreakData(5, 1, LocalDate.now()))
        dataStore.updateStreakData(StreakData(20, 4, LocalDate.now()))
        val data = dataStore.streakData.first()
        assertEquals(20, data.streakCount)
        assertEquals(4, data.freezesBanked)
    }

    @Test
    fun updateStreakDataWithNullDate() = runTest {
        dataStore.updateStreakData(StreakData(3, 0, null))
        val data = dataStore.streakData.first()
        assertEquals(3, data.streakCount)
        assertNull(data.lastFreezeEarnDate)
    }

    @Test
    fun clearResetsToDefaults() = runTest {
        dataStore.updateStreakData(StreakData(100, 5, LocalDate.now()))
        dataStore.clear()
        val data = dataStore.streakData.first()
        assertEquals(0, data.streakCount)
        assertEquals(0, data.freezesBanked)
        assertNull(data.lastFreezeEarnDate)
    }
}
