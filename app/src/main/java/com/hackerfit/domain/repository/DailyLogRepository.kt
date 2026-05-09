package com.hackerfit.domain.repository

import com.hackerfit.domain.model.DailyLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyLogRepository {
    fun getAllLogs(): Flow<List<DailyLog>>
    fun getLogsInRange(start: LocalDate, end: LocalDate): Flow<List<DailyLog>>
    suspend fun getLogForDate(date: LocalDate): DailyLog?
    suspend fun saveLog(log: DailyLog)
    suspend fun hasCompletedToday(): Boolean
    suspend fun getConsecutiveDays(): Int
}
