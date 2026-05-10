package com.hackerfit.data.local.db.dao

import androidx.room.*
import com.hackerfit.data.local.db.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_log ORDER BY date DESC")
    fun getAllLogs(): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_log WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getLogsInRange(start: LocalDate, end: LocalDate): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_log WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: LocalDate): DailyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLog(log: DailyLogEntity)

    @Query("SELECT COUNT(*) FROM daily_log WHERE date = :date AND completed = 1")
    suspend fun hasCompletedOnDate(date: LocalDate): Int

    @Query("SELECT * FROM daily_log WHERE completed = 1 ORDER BY date DESC")
    fun getCompletedLogs(): Flow<List<DailyLogEntity>>

    @Query("DELETE FROM daily_log")
    suspend fun deleteAll()

    @Query("SELECT * FROM daily_log ORDER BY date DESC")
    suspend fun getAllLogsList(): List<DailyLogEntity>

    @Query("SELECT * FROM daily_log WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    suspend fun getLogsInRangeList(start: LocalDate, end: LocalDate): List<DailyLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<DailyLogEntity>)

    @Query("DELETE FROM daily_log WHERE id = :id")
    suspend fun deleteById(id: Long)
}
