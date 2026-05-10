package com.hackerfit.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hackerfit.data.local.db.dao.AssessmentLogDao
import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class AssessmentLogDaoTest {

    private lateinit var db: HackerFitDatabase
    private lateinit var dao: AssessmentLogDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HackerFitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.assessmentLogDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndRead() = runTest {
        val entity = AssessmentLogEntity(date = LocalDate.now(), fromRung = 5, toRung = 6, passed = true, notes = "test")
        dao.saveAssessment(entity)
        val assessments = dao.getAllAssessments().first()
        assertEquals(1, assessments.size)
        assertEquals(5, assessments[0].fromRung)
        assertTrue(assessments[0].passed)
        assertEquals("test", assessments[0].notes)
    }

    @Test
    fun getAllAssessmentsOrderedByDateDesc() = runTest {
        dao.saveAssessment(AssessmentLogEntity(date = LocalDate.of(2025, 1, 1), fromRung = 1, toRung = 2, passed = true))
        dao.saveAssessment(AssessmentLogEntity(date = LocalDate.of(2025, 3, 1), fromRung = 2, toRung = 3, passed = true))
        val assessments = dao.getAllAssessments().first()
        assertEquals(LocalDate.of(2025, 3, 1), assessments[0].date)
        assertEquals(LocalDate.of(2025, 1, 1), assessments[1].date)
    }

    @Test
    fun getLastAssessmentDateReturnsLatest() = runTest {
        dao.saveAssessment(AssessmentLogEntity(date = LocalDate.of(2025, 1, 1), fromRung = 1, toRung = 2, passed = true))
        dao.saveAssessment(AssessmentLogEntity(date = LocalDate.of(2025, 6, 15), fromRung = 2, toRung = 3, passed = false))
        assertEquals(LocalDate.of(2025, 6, 15), dao.getLastAssessmentDate())
    }

    @Test
    fun getLastAssessmentDateReturnsNullWhenEmpty() = runTest {
        assertNull(dao.getLastAssessmentDate())
    }

    @Test
    fun deleteAllClearsAssessments() = runTest {
        dao.saveAssessment(AssessmentLogEntity(date = LocalDate.now(), fromRung = 1, toRung = 2, passed = true))
        dao.deleteAll()
        assertTrue(dao.getAllAssessments().first().isEmpty())
    }

    @Test
    fun insertAllAddsMultiple() = runTest {
        val list = listOf(
            AssessmentLogEntity(date = LocalDate.of(2025, 1, 1), fromRung = 1, toRung = 2, passed = true),
            AssessmentLogEntity(date = LocalDate.of(2025, 2, 1), fromRung = 2, toRung = 3, passed = false)
        )
        dao.insertAll(list)
        assertEquals(2, dao.getAllAssessments().first().size)
    }

    @Test
    fun getAllAssessmentsListReturnsList() = runTest {
        dao.saveAssessment(AssessmentLogEntity(date = LocalDate.now(), fromRung = 1, toRung = 2, passed = true))
        val list = dao.getAllAssessmentsList()
        assertEquals(1, list.size)
    }
}
