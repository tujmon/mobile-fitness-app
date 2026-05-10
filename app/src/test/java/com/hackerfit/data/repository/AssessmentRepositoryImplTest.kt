package com.hackerfit.data.repository

import com.hackerfit.data.local.db.dao.AssessmentLogDao
import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class AssessmentRepositoryImplTest {

    private val assessmentsFlow = MutableStateFlow<List<AssessmentLogEntity>>(emptyList())
    private var lastSavedAssessment: AssessmentLogEntity? = null
    private var lastAssessmentDate: LocalDate? = null

    private val fakeDao = object : AssessmentLogDao {
        override fun getAllAssessments() = assessmentsFlow.map { it }

        override suspend fun saveAssessment(assessment: AssessmentLogEntity) {
            lastSavedAssessment = assessment
            assessmentsFlow.value = assessmentsFlow.value + assessment
        }

        override suspend fun getLastAssessmentDate() = lastAssessmentDate

        override suspend fun deleteAll() {
            assessmentsFlow.value = emptyList()
        }

        override suspend fun getAllAssessmentsList() = assessmentsFlow.value

        override suspend fun insertAll(assessments: List<AssessmentLogEntity>) {
            assessmentsFlow.value = assessmentsFlow.value + assessments
        }

        override suspend fun deleteById(id: Long) {
            assessmentsFlow.value = assessmentsFlow.value.filter { it.id != id }
        }
    }

    private lateinit var repository: AssessmentRepositoryImpl

    @Before
    fun setup() {
        repository = AssessmentRepositoryImpl(fakeDao)
        lastSavedAssessment = null
        lastAssessmentDate = null
    }

    @Test
    fun `getAllAssessments maps entities to domain`() = runTest {
        val date = LocalDate.of(2025, 3, 1)
        assessmentsFlow.value = listOf(
            AssessmentLogEntity(1, date, 3, 4, true, "notes")
        )
        val result = repository.getAllAssessments().first()
        assertEquals(1, result.size)
        assertEquals(date, result[0].date)
        assertEquals(3, result[0].fromRung)
        assertEquals(4, result[0].toRung)
        assertTrue(result[0].passed)
        assertEquals("notes", result[0].notes)
    }

    @Test
    fun `getAllAssessments returns empty for no data`() = runTest {
        val result = repository.getAllAssessments().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `saveAssessment converts domain to entity`() = runTest {
        val date = LocalDate.now()
        repository.saveAssessment(
            com.hackerfit.domain.model.AssessmentLog(1, date, 5, 6, true, "test")
        )
        assertNotNull(lastSavedAssessment)
        assertEquals(1L, lastSavedAssessment!!.id)
        assertEquals(date, lastSavedAssessment!!.date)
        assertEquals(5, lastSavedAssessment!!.fromRung)
        assertTrue(lastSavedAssessment!!.passed)
        assertEquals("test", lastSavedAssessment!!.notes)
    }

    @Test
    fun `saveAssessment with id 0 preserves 0`() = runTest {
        repository.saveAssessment(
            com.hackerfit.domain.model.AssessmentLog(0, LocalDate.now(), 1, 2, true, null)
        )
        assertEquals(0L, lastSavedAssessment!!.id)
    }

    @Test
    fun `saveAssessment with non-zero id preserves id`() = runTest {
        repository.saveAssessment(
            com.hackerfit.domain.model.AssessmentLog(42, LocalDate.now(), 1, 2, true, null)
        )
        assertEquals(42L, lastSavedAssessment!!.id)
    }

    @Test
    fun `getLastAssessmentDate delegates to dao`() = runTest {
        val date = LocalDate.of(2025, 5, 1)
        lastAssessmentDate = date
        assertEquals(date, repository.getLastAssessmentDate())
    }

    @Test
    fun `getLastAssessmentDate returns null when no assessments`() = runTest {
        assertNull(repository.getLastAssessmentDate())
    }
}
