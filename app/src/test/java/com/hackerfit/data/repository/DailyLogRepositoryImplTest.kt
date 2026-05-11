package com.hackerfit.data.repository

import com.hackerfit.data.local.db.dao.DailyLogDao
import com.hackerfit.data.local.db.entity.DailyLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DailyLogRepositoryImplTest {

    private val logsFlow = MutableStateFlow<List<DailyLogEntity>>(emptyList())

    private val fakeDao = object : DailyLogDao {
        override fun getAllLogs() = logsFlow.map { it }

        override fun getLogsInRange(start: LocalDate, end: LocalDate) =
            logsFlow.map { logs -> logs.filter { it.date in start..end } }

        override suspend fun getLogForDate(date: LocalDate) =
            logsFlow.value.find { it.date == date }

        override suspend fun saveLog(log: DailyLogEntity) {
            val current = logsFlow.value.toMutableList()
            val idx = current.indexOfFirst { it.date == log.date }
            if (idx >= 0) current[idx] = log else current.add(log)
            logsFlow.value = current
        }

        override suspend fun hasCompletedOnDate(date: LocalDate) =
            logsFlow.value.count { it.date == date && it.completed }

        override fun observeCompletedOnDate(date: LocalDate) =
            logsFlow.map { logs -> logs.any { it.date == date && it.completed } }

        override fun getCompletedLogs() = logsFlow.map { logs -> logs.filter { it.completed } }

        override suspend fun deleteAll() {
            logsFlow.value = emptyList()
        }

        override suspend fun getAllLogsList() = logsFlow.value

        override suspend fun getLogsInRangeList(start: LocalDate, end: LocalDate) =
            logsFlow.value.filter { it.date in start..end }

        override suspend fun insertAll(logs: List<DailyLogEntity>) {
            logsFlow.value = logsFlow.value + logs
        }

        override suspend fun deleteById(id: Long) {
            logsFlow.value = logsFlow.value.filter { it.id != id }
        }
    }

    private lateinit var repository: DailyLogRepositoryImpl

    @Before
    fun setup() {
        repository = DailyLogRepositoryImpl(fakeDao)
    }

    @Test
    fun `getAllLogs returns empty initially`() = runTest {
        assertTrue(repository.getAllLogs().first().isEmpty())
    }

    @Test
    fun `getAllLogs maps entities to domain`() = runTest {
        val date = LocalDate.of(2025, 3, 1)
        logsFlow.value = listOf(DailyLogEntity(1, date, 5, true, date))
        val logs = repository.getAllLogs().first()
        assertEquals(1, logs.size)
        assertEquals(date, logs[0].date)
        assertEquals(5, logs[0].rung)
        assertTrue(logs[0].completed)
    }

    @Test
    fun `getLogsInRange filters by date range`() = runTest {
        logsFlow.value = listOf(
            DailyLogEntity(1, LocalDate.of(2025, 3, 1), 1, true, null),
            DailyLogEntity(2, LocalDate.of(2025, 3, 15), 1, true, null),
            DailyLogEntity(3, LocalDate.of(2025, 3, 31), 1, true, null)
        )
        val logs = repository.getLogsInRange(
            LocalDate.of(2025, 3, 5),
            LocalDate.of(2025, 3, 20)
        ).first()
        assertEquals(1, logs.size)
        assertEquals(LocalDate.of(2025, 3, 15), logs[0].date)
    }

    @Test
    fun `getLogForDate returns correct log`() = runTest {
        val target = LocalDate.of(2025, 3, 10)
        logsFlow.value = listOf(
            DailyLogEntity(1, LocalDate.of(2025, 3, 9), 1, true, null),
            DailyLogEntity(2, target, 5, true, target),
            DailyLogEntity(3, LocalDate.of(2025, 3, 11), 1, true, null)
        )
        val log = repository.getLogForDate(target)
        assertNotNull(log)
        assertEquals(5, log!!.rung)
    }

    @Test
    fun `getLogForDate returns null for missing`() = runTest {
        assertNull(repository.getLogForDate(LocalDate.of(2020, 1, 1)))
    }

    @Test
    fun `saveLog creates new entry`() = runTest {
        repository.saveLog(
            com.hackerfit.domain.model.DailyLog(date = LocalDate.now(), rung = 3, completed = true, completedAt = LocalDate.now())
        )
        assertEquals(1, logsFlow.value.size)
        assertEquals(3, logsFlow.value[0].rung)
    }

    @Test
    fun `saveLog replaces existing by date`() = runTest {
        val date = LocalDate.now()
        logsFlow.value = listOf(DailyLogEntity(1, date, 3, false, null))
        repository.saveLog(
            com.hackerfit.domain.model.DailyLog(date = date, rung = 5, completed = true, completedAt = date)
        )
        assertEquals(1, logsFlow.value.size)
        assertEquals(5, logsFlow.value[0].rung)
        assertTrue(logsFlow.value[0].completed)
    }

    @Test
    fun `hasCompletedToday returns true when completed`() = runTest {
        logsFlow.value = listOf(DailyLogEntity(1, LocalDate.now(), 1, true, LocalDate.now()))
        assertTrue(repository.hasCompletedToday())
    }

    @Test
    fun `hasCompletedToday returns false when not completed`() = runTest {
        logsFlow.value = listOf(DailyLogEntity(1, LocalDate.now(), 1, false, null))
        assertFalse(repository.hasCompletedToday())
    }

    @Test
    fun `hasCompletedToday returns false when no log today`() = runTest {
        logsFlow.value = listOf(DailyLogEntity(1, LocalDate.now().minusDays(1), 1, true, LocalDate.now().minusDays(1)))
        assertFalse(repository.hasCompletedToday())
    }

    @Test
    fun `getConsecutiveDays counts consecutive completed days`() = runTest {
        val today = LocalDate.now()
        logsFlow.value = listOf(
            DailyLogEntity(1, today, 1, true, today),
            DailyLogEntity(2, today.minusDays(1), 1, true, today.minusDays(1)),
            DailyLogEntity(3, today.minusDays(2), 1, true, today.minusDays(2))
        )
        assertEquals(3, repository.getConsecutiveDays())
    }

    @Test
    fun `getConsecutiveDays stops at gap`() = runTest {
        val today = LocalDate.now()
        logsFlow.value = listOf(
            DailyLogEntity(1, today, 1, true, today),
            DailyLogEntity(2, today.minusDays(1), 1, false, null),
            DailyLogEntity(3, today.minusDays(2), 1, true, today.minusDays(2))
        )
        assertEquals(1, repository.getConsecutiveDays())
    }

    @Test
    fun `getConsecutiveDays is 0 when today not completed`() = runTest {
        logsFlow.value = listOf(
            DailyLogEntity(1, LocalDate.now().minusDays(1), 1, true, LocalDate.now().minusDays(1))
        )
        assertEquals(0, repository.getConsecutiveDays())
    }

    @Test
    fun `getConsecutiveDays is 0 when empty`() = runTest {
        assertEquals(0, repository.getConsecutiveDays())
    }
}
