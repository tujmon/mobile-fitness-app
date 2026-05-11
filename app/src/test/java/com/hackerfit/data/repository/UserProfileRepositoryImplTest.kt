package com.hackerfit.data.repository

import com.hackerfit.data.local.db.dao.AssessmentLogDao
import com.hackerfit.data.local.db.dao.UserProfileDao
import com.hackerfit.data.local.db.entity.UserProfileEntity
import com.hackerfit.domain.model.Phase
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class UserProfileRepositoryImplTest {

    private val profileFlow = MutableStateFlow<UserProfileEntity?>(null)
    private var savedProfile: UserProfileEntity? = null
    private var updatedRung: Triple<Int, String, LocalDate>? = null
    private var completedOnboarding = false
    private var reminderTime: Pair<Int, Int>? = null
    private var reminderCleared = false

    private val fakeDao = object : UserProfileDao {
        override fun getProfile() = profileFlow.map { it }

        override suspend fun saveProfile(profile: UserProfileEntity) {
            savedProfile = profile
            profileFlow.value = profile
        }

        override suspend fun updateRung(rung: Int, phase: String, date: LocalDate) {
            updatedRung = Triple(rung, phase, date)
            profileFlow.value = profileFlow.value?.copy(currentRung = rung, phase = phase, rungStartDate = date)
        }

        override suspend fun completeOnboarding() {
            completedOnboarding = true
            profileFlow.value = profileFlow.value?.copy(onboardingComplete = true)
        }

        override suspend fun setReminderTime(hour: Int, minute: Int) {
            reminderTime = Pair(hour, minute)
            profileFlow.value = profileFlow.value?.copy(dailyReminderHour = hour, dailyReminderMinute = minute)
        }

        override suspend fun clearReminderTime() {
            reminderCleared = true
            profileFlow.value = profileFlow.value?.copy(dailyReminderHour = null, dailyReminderMinute = null)
        }

        override suspend fun getProfileOnce() = profileFlow.value
    }

    private lateinit var repository: UserProfileRepositoryImpl
    private val fakeAssessmentLogDao = mockk<AssessmentLogDao>(relaxed = true)

    @Before
    fun setup() {
        savedProfile = null
        updatedRung = null
        completedOnboarding = false
        reminderTime = null
        reminderCleared = false
        repository = UserProfileRepositoryImpl(fakeDao, fakeAssessmentLogDao)
    }

    @Test
    fun `getProfile returns null when no profile`() = runTest {
        assertNull(repository.getProfile().first())
    }

    @Test
    fun `getProfile maps introductory entity to domain`() = runTest {
        val date = LocalDate.of(2025, 1, 15)
        profileFlow.value = UserProfileEntity(1, 5, "introductory", date, 8, 30, true)
        val profile = repository.getProfile().first()
        assertNotNull(profile)
        assertEquals(5, profile!!.currentRung)
        assertEquals(Phase.INTRODUCTORY, profile.phase)
        assertEquals(date, profile.rungStartDate)
        assertEquals(8, profile.dailyReminderHour)
        assertTrue(profile.onboardingComplete)
    }

    @Test
    fun `getProfile maps lifetime entity to domain`() = runTest {
        profileFlow.value = UserProfileEntity(1, 20, "lifetime", LocalDate.now(), null, null, true)
        val profile = repository.getProfile().first()
        assertEquals(Phase.LIFETIME, profile!!.phase)
    }

    @Test
    fun `saveProfile converts domain introductory to entity`() = runTest {
        val date = LocalDate.of(2025, 2, 1)
        repository.saveProfile(
            com.hackerfit.domain.model.UserProfile(5, Phase.INTRODUCTORY, date, 8, 30, true)
        )
        assertNotNull(savedProfile)
        assertEquals(5, savedProfile!!.currentRung)
        assertEquals("introductory", savedProfile!!.phase)
        assertEquals(date, savedProfile!!.rungStartDate)
        assertEquals(8, savedProfile!!.dailyReminderHour)
        assertEquals(30, savedProfile!!.dailyReminderMinute)
        assertTrue(savedProfile!!.onboardingComplete)
    }

    @Test
    fun `saveProfile converts domain lifetime to entity`() = runTest {
        repository.saveProfile(
            com.hackerfit.domain.model.UserProfile(20, Phase.LIFETIME, LocalDate.now(), null, null, true)
        )
        assertEquals("lifetime", savedProfile!!.phase)
    }

    @Test
    fun `saveProfile maps null reminders to entity`() = runTest {
        repository.saveProfile(
            com.hackerfit.domain.model.UserProfile(1, Phase.INTRODUCTORY, LocalDate.now(), null, null, false)
        )
        assertNull(savedProfile!!.dailyReminderHour)
        assertNull(savedProfile!!.dailyReminderMinute)
    }

    @Test
    fun `updateRung with introductory rung`() = runTest {
        repository.updateRung(10)
        assertNotNull(updatedRung)
        assertEquals(10, updatedRung!!.first)
        assertEquals("introductory", updatedRung!!.second)
    }

    @Test
    fun `updateRung with lifetime rung`() = runTest {
        repository.updateRung(20)
        assertNotNull(updatedRung)
        assertEquals(20, updatedRung!!.first)
        assertEquals("lifetime", updatedRung!!.second)
    }

    @Test
    fun `updateRung boundary rung 15 is introductory`() = runTest {
        repository.updateRung(15)
        assertEquals("introductory", updatedRung!!.second)
    }

    @Test
    fun `updateRung boundary rung 16 is lifetime`() = runTest {
        repository.updateRung(16)
        assertEquals("lifetime", updatedRung!!.second)
    }

    @Test
    fun `completeOnboarding delegates to dao`() = runTest {
        repository.completeOnboarding()
        assertTrue(completedOnboarding)
    }

    @Test
    fun `setReminderTime delegates to dao`() = runTest {
        repository.setReminderTime(9, 30)
        assertNotNull(reminderTime)
        assertEquals(9, reminderTime!!.first)
        assertEquals(30, reminderTime!!.second)
    }

    @Test
    fun `clearReminderTime delegates to dao`() = runTest {
        repository.clearReminderTime()
        assertTrue(reminderCleared)
    }
}
