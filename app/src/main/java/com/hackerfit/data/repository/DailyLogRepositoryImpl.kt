package com.hackerfit.data.repository

import com.hackerfit.data.local.db.dao.DailyLogDao
import com.hackerfit.data.local.db.entity.DailyLogEntity
import com.hackerfit.data.mapper.toDomain
import com.hackerfit.domain.model.DailyLog
import com.hackerfit.domain.repository.DailyLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyLogRepositoryImpl @Inject constructor(
    private val dao: DailyLogDao
) : DailyLogRepository {

    override fun getAllLogs(): Flow<List<DailyLog>> {
        return dao.getAllLogs().map { list -> list.map { it.toDomain() } }
    }

    override fun getLogsInRange(start: LocalDate, end: LocalDate): Flow<List<DailyLog>> {
        return dao.getLogsInRange(start, end).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getLogForDate(date: LocalDate): DailyLog? {
        return dao.getLogForDate(date)?.toDomain()
    }

    override suspend fun saveLog(log: DailyLog) {
        dao.saveLog(
            DailyLogEntity(
                id = if (log.id == 0L) 0 else log.id,
                date = log.date,
                rung = log.rung,
                completed = log.completed,
                completedAt = log.completedAt
            )
        )
    }

    override suspend fun hasCompletedToday(): Boolean {
        return dao.hasCompletedOnDate(LocalDate.now()) > 0
    }

    override fun observeCompletedToday(): Flow<Boolean> {
        return dao.observeCompletedOnDate(LocalDate.now())
    }

    override suspend fun getConsecutiveDays(): Int {
        var count = 0
        var date = LocalDate.now()
        while (dao.hasCompletedOnDate(date) > 0) {
            count++
            date = date.minusDays(1)
        }
        return count
    }

    override suspend fun deleteLog(id: Long) {
        dao.deleteById(id)
    }
}
