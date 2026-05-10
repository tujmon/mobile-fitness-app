package com.hackerfit.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hackerfit.data.local.db.dao.UserProfileDao
import com.hackerfit.data.local.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class UserProfileDaoTest {

    private lateinit var db: HackerFitDatabase
    private lateinit var dao: UserProfileDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HackerFitDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.userProfileDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun getProfileReturnsNullWhenEmpty() = runTest {
        assertNull(dao.getProfile().first())
    }

    @Test
    fun saveAndGetProfile() = runTest {
        val entity = UserProfileEntity(
            currentRung = 5,
            phase = "introductory",
            rungStartDate = LocalDate.of(2025, 1, 1),
            dailyReminderHour = 8,
            dailyReminderMinute = 30,
            onboardingComplete = true
        )
        dao.saveProfile(entity)
        val profile = dao.getProfile().first()
        assertNotNull(profile)
        assertEquals(5, profile!!.currentRung)
        assertEquals("introductory", profile.phase)
        assertEquals(8, profile.dailyReminderHour)
        assertTrue(profile.onboardingComplete)
    }

    @Test
    fun saveProfileReplacesExisting() = runTest {
        dao.saveProfile(UserProfileEntity(currentRung = 1, phase = "introductory"))
        dao.saveProfile(UserProfileEntity(currentRung = 10, phase = "lifetime"))
        val profile = dao.getProfile().first()
        assertEquals(10, profile!!.currentRung)
        assertEquals("lifetime", profile.phase)
    }

    @Test
    fun updateRung() = runTest {
        dao.saveProfile(UserProfileEntity(currentRung = 1, phase = "introductory"))
        dao.updateRung(15, "introductory", LocalDate.now())
        val profile = dao.getProfile().first()
        assertEquals(15, profile!!.currentRung)
    }

    @Test
    fun completeOnboarding() = runTest {
        dao.saveProfile(UserProfileEntity(onboardingComplete = false))
        dao.completeOnboarding()
        assertTrue(dao.getProfile().first()!!.onboardingComplete)
    }

    @Test
    fun setReminderTime() = runTest {
        dao.saveProfile(UserProfileEntity())
        dao.setReminderTime(9, 45)
        val profile = dao.getProfile().first()
        assertEquals(9, profile!!.dailyReminderHour)
        assertEquals(45, profile.dailyReminderMinute)
    }

    @Test
    fun clearReminderTime() = runTest {
        dao.saveProfile(UserProfileEntity(dailyReminderHour = 8, dailyReminderMinute = 0))
        dao.clearReminderTime()
        val profile = dao.getProfile().first()
        assertNull(profile!!.dailyReminderHour)
        assertNull(profile.dailyReminderMinute)
    }

    @Test
    fun getProfileOnceReturnsSameAsFlow() = runTest {
        dao.saveProfile(UserProfileEntity(currentRung = 7))
        val fromFlow = dao.getProfile().first()
        val fromOnce = dao.getProfileOnce()
        assertEquals(fromFlow!!.currentRung, fromOnce!!.currentRung)
    }

    @Test
    fun getProfileOnceReturnsNullWhenEmpty() = runTest {
        assertNull(dao.getProfileOnce())
    }
}
