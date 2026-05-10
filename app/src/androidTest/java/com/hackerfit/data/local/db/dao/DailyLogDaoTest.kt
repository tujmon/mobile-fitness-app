package com.hackerfit.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hackerfit.data.local.db.dao.DailyLogDao
import com.hackerfit.data.local.db.entity.DailyLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DailyLogDaoTest {

    private lateinit var db: HackerFitDatabase
    private lateinit var dao: DailyLogDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HackerFitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dailyLogDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndRead() = runTest {
        val entity = DailyLogEntity(date = LocalDate.now(), rung = 5, completed = true, completedAt = LocalDate.now())
        dao.saveLog(entity)
        val logs = dao.getAllLogs().first()
        assertEquals(1, logs.size)
        assertEquals(5, logs[0].rung)
        assertTrue(logs[0].completed)
    }

    @Test
    fun insertReplacesBySameDate() = runTest {
        val date = LocalDate.now()
        dao.saveLog(DailyLogEntity(date = date, rung = 3, completed = false, completedAt = null))
        dao.saveLog(DailyLogEntity(date = date, rung = 5, completed = true, completedAt = date))
        val logs = dao.getAllLogs().first()
        assertEquals(1, logs.size)
        assertEquals(5, logs[0].rung)
    }

    @Test
    fun getLogForDateReturnsCorrect() = runTest {
        val target = LocalDate.of(2025, 3, 15)
        dao.saveLog(DailyLogEntity(date = target, rung = 10, completed = true, completedAt = target))
        dao.saveLog(DailyLogEntity(date = LocalDate.now(), rung = 1, completed = false, completedAt = null))
        val log = dao.getLogForDate(target)
        assertNotNull(log)
        assertEquals(10, log!!.rung)
    }

    @Test
    fun getLogForDateReturnsNullForMissing() = runTest {
        assertNull(dao.getLogForDate(LocalDate.of(2020, 1, 1)))
    }

    @Test
    fun hasCompletedOnDateReturns1WhenCompleted() = runTest {
        val date = LocalDate.now()
        dao.saveLog(DailyLogEntity(date = date, rung = 1, completed = true, completedAt = date))
        assertEquals(1, dao.hasCompletedOnDate(date))
    }

    @Test
    fun hasCompletedOnDateReturns0WhenNotCompleted() = runTest {
        val date = LocalDate.now()
        dao.saveLog(DailyLogEntity(date = date, rung = 1, completed = false, completedAt = null))
        assertEquals(0, dao.hasCompletedOnDate(date))
    }

    @Test
    fun hasCompletedOnDateReturns0WhenNoLog() = runTest {
        assertEquals(0, dao.hasCompletedOnDate(LocalDate.now()))
    }

    @Test
    fun getLogsInRangeFiltersCorrectly() = runTest {
        dao.saveLog(DailyLogEntity(date = LocalDate.of(2025, 3, 1), rung = 1, completed = true, completedAt = LocalDate.of(2025, 3, 1)))
        dao.saveLog(DailyLogEntity(date = LocalDate.of(2025, 3, 15), rung = 1, completed = true, completedAt = LocalDate.of(2025, 3, 15)))
        dao.saveLog(DailyLogEntity(date = LocalDate.of(2025, 3, 31), rung = 1, completed = true, completedAt = LocalDate.of(2025, 3, 31)))
        val logs = dao.getLogsInRange(LocalDate.of(2025, 3, 5), LocalDate.of(2025, 3, 20)).first()
        assertEquals(1, logs.size)
        assertEquals(LocalDate.of(2025, 3, 15), logs[0].date)
    }

    @Test
    fun getCompletedLogsReturnsOnlyCompleted() = runTest {
        dao.saveLog(DailyLogEntity(date = LocalDate.now(), rung = 1, completed = true, completedAt = LocalDate.now()))
        dao.saveLog(DailyLogEntity(date = LocalDate.now().minusDays(1), rung = 1, completed = false, completedAt = null))
        val completed = dao.getCompletedLogs().first()
        assertEquals(1, completed.size)
    }

    @Test
    fun deleteAllClearsAll() = runTest {
        dao.saveLog(DailyLogEntity(date = LocalDate.now(), rung = 1, completed = true, completedAt = LocalDate.now()))
        dao.deleteAll()
        assertTrue(dao.getAllLogs().first().isEmpty())
    }

    @Test
    fun getAllLogsListReturnsList() = runTest {
        dao.saveLog(DailyLogEntity(date = LocalDate.now(), rung = 1, completed = true, completedAt = LocalDate.now()))
        val list = dao.getAllLogsList()
        assertEquals(1, list.size)
    }

    @Test
    fun insertAllAddsMultiple() = runTest {
        val logs = listOf(
            DailyLogEntity(date = LocalDate.of(2025, 1, 1), rung = 1, completed = true, completedAt = LocalDate.of(2025, 1, 1)),
            DailyLogEntity(date = LocalDate.of(2025, 1, 2), rung = 2, completed = true, completedAt = LocalDate.of(2025, 1, 2))
        )
        dao.insertAll(logs)
        assertEquals(2, dao.getAllLogs().first().size)
    }

    @Test
    fun getAllLogsOrderedByDateDesc() = runTest {
        dao.saveLog(DailyLogEntity(date = LocalDate.of(2025, 1, 1), rung = 1, completed = true, completedAt = null))
        dao.saveLog(DailyLogEntity(date = LocalDate.of(2025, 3, 1), rung = 2, completed = true, completedAt = null))
        val logs = dao.getAllLogs().first()
        assertEquals(LocalDate.of(2025, 3, 1), logs[0].date)
        assertEquals(LocalDate.of(2025, 1, 1), logs[1].date)
    }
}
