package com.hackerfit.domain.repository

import com.hackerfit.domain.model.AssessmentLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AssessmentRepository {
    fun getAllAssessments(): Flow<List<AssessmentLog>>
    suspend fun saveAssessment(assessment: AssessmentLog)
    suspend fun getLastAssessmentDate(): LocalDate?
}
