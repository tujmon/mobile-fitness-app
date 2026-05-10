package com.hackerfit.data.repository

import com.hackerfit.data.local.db.dao.AssessmentLogDao
import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import com.hackerfit.data.mapper.toDomain
import com.hackerfit.domain.model.AssessmentLog
import com.hackerfit.domain.repository.AssessmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssessmentRepositoryImpl @Inject constructor(
    private val dao: AssessmentLogDao
) : AssessmentRepository {

    override fun getAllAssessments(): Flow<List<AssessmentLog>> {
        return dao.getAllAssessments().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveAssessment(assessment: AssessmentLog) {
        dao.saveAssessment(
            AssessmentLogEntity(
                id = if (assessment.id == 0L) 0 else assessment.id,
                date = assessment.date,
                fromRung = assessment.fromRung,
                toRung = assessment.toRung,
                passed = assessment.passed,
                notes = assessment.notes
            )
        )
    }

    override suspend fun getLastAssessmentDate(): LocalDate? {
        return dao.getLastAssessmentDate()
    }

    override suspend fun deleteAssessment(id: Long) {
        dao.deleteById(id)
    }
}
