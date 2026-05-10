package com.hackerfit.data.local.db.dao

import androidx.room.*
import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AssessmentLogDao {
    @Query("SELECT * FROM assessment_log ORDER BY date DESC")
    fun getAllAssessments(): Flow<List<AssessmentLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAssessment(assessment: AssessmentLogEntity)

    @Query("SELECT date FROM assessment_log ORDER BY date DESC LIMIT 1")
    suspend fun getLastAssessmentDate(): LocalDate?

    @Query("DELETE FROM assessment_log")
    suspend fun deleteAll()

    @Query("SELECT * FROM assessment_log ORDER BY date DESC")
    suspend fun getAllAssessmentsList(): List<AssessmentLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assessments: List<AssessmentLogEntity>)

    @Query("DELETE FROM assessment_log WHERE id = :id")
    suspend fun deleteById(id: Long)
}
