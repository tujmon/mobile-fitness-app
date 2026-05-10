package com.hackerfit.data.export

import com.hackerfit.data.local.db.entity.AssessmentLogEntity
import com.hackerfit.data.local.db.entity.DailyLogEntity
import com.hackerfit.data.local.db.entity.UserProfileEntity
import com.hackerfit.domain.model.StreakData
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class DataExporterImporterTest {

    private val testProfile = UserProfileEntity(
        id = 1,
        currentRung = 10,
        phase = "introductory",
        rungStartDate = LocalDate.of(2025, 1, 15),
        dailyReminderHour = 8,
        dailyReminderMinute = 30,
        onboardingComplete = true
    )

    private val testLogs = listOf(
        DailyLogEntity(1, LocalDate.of(2025, 3, 1), 10, true, LocalDate.of(2025, 3, 1)),
        DailyLogEntity(2, LocalDate.of(2025, 3, 2), 10, true, LocalDate.of(2025, 3, 2)),
        DailyLogEntity(3, LocalDate.of(2025, 3, 3), 10, false, null)
    )

    private val testAssessments = listOf(
        AssessmentLogEntity(1, LocalDate.of(2025, 2, 1), 5, 6, true, "Easy"),
        AssessmentLogEntity(2, LocalDate.of(2025, 2, 15), 9, 10, false, null)
    )

    private val testStreak = StreakData(15, 3, LocalDate.of(2025, 3, 1))

    @Test
    fun `export produces valid JSON`() {
        val json = DataExporter.exportToJson(testProfile, testLogs, testAssessments, testStreak)
        val root = JSONObject(json)
        assertEquals(1, root.getInt("version"))
        assertEquals("HackerFit", root.getString("app"))
    }

    @Test
    fun `export contains profile data`() {
        val json = DataExporter.exportToJson(testProfile, testLogs, testAssessments, testStreak)
        val root = JSONObject(json)
        val profile = root.getJSONObject("profile")
        assertEquals(10, profile.getInt("currentRung"))
        assertEquals("introductory", profile.getString("phase"))
        assertEquals("2025-01-15", profile.getString("rungStartDate"))
        assertEquals(8, profile.getInt("dailyReminderHour"))
        assertEquals(30, profile.getInt("dailyReminderMinute"))
        assertTrue(profile.getBoolean("onboardingComplete"))
    }

    @Test
    fun `export contains streak data`() {
        val json = DataExporter.exportToJson(testProfile, testLogs, testAssessments, testStreak)
        val root = JSONObject(json)
        val streak = root.getJSONObject("streak")
        assertEquals(15, streak.getInt("streakCount"))
        assertEquals(3, streak.getInt("freezesBanked"))
        assertEquals("2025-03-01", streak.getString("lastFreezeEarnDate"))
    }

    @Test
    fun `export contains daily logs`() {
        val json = DataExporter.exportToJson(testProfile, testLogs, testAssessments, testStreak)
        val root = JSONObject(json)
        val logs = root.getJSONArray("dailyLogs")
        assertEquals(3, logs.length())
        assertEquals("2025-03-01", logs.getJSONObject(0).getString("date"))
        assertTrue(logs.getJSONObject(0).getBoolean("completed"))
        assertFalse(logs.getJSONObject(2).getBoolean("completed"))
    }

    @Test
    fun `export contains assessment logs`() {
        val json = DataExporter.exportToJson(testProfile, testLogs, testAssessments, testStreak)
        val root = JSONObject(json)
        val assessments = root.getJSONArray("assessmentLogs")
        assertEquals(2, assessments.length())
        assertEquals(5, assessments.getJSONObject(0).getInt("fromRung"))
        assertTrue(assessments.getJSONObject(0).getBoolean("passed"))
        assertFalse(assessments.getJSONObject(1).getBoolean("passed"))
    }

    @Test
    fun `export with null profile produces null in JSON`() {
        val json = DataExporter.exportToJson(null, emptyList(), emptyList(), StreakData(0, 0, null))
        val root = JSONObject(json)
        assertTrue(root.isNull("profile"))
    }

    @Test
    fun `export with null reminder handles correctly`() {
        val profile = testProfile.copy(dailyReminderHour = null, dailyReminderMinute = null)
        val json = DataExporter.exportToJson(profile, emptyList(), emptyList(), StreakData(0, 0, null))
        val root = JSONObject(json)
        val p = root.getJSONObject("profile")
        assertTrue(p.isNull("dailyReminderHour"))
        assertTrue(p.isNull("dailyReminderMinute"))
    }

    @Test
    fun `import parses exported data correctly`() {
        val json = DataExporter.exportToJson(testProfile, testLogs, testAssessments, testStreak)
        val imported = DataImporter.parseFromJson(json)

        assertNotNull(imported.profile)
        assertEquals(10, imported.profile!!.currentRung)
        assertEquals("introductory", imported.profile!!.phase)
        assertEquals(LocalDate.of(2025, 1, 15), imported.profile!!.rungStartDate)
        assertEquals(8, imported.profile!!.dailyReminderHour)
        assertEquals(30, imported.profile!!.dailyReminderMinute)
        assertTrue(imported.profile!!.onboardingComplete)
    }

    @Test
    fun `import parses streak correctly`() {
        val json = DataExporter.exportToJson(testProfile, testLogs, testAssessments, testStreak)
        val imported = DataImporter.parseFromJson(json)

        assertEquals(15, imported.streak.streakCount)
        assertEquals(3, imported.streak.freezesBanked)
        assertEquals(LocalDate.of(2025, 3, 1), imported.streak.lastFreezeEarnDate)
    }

    @Test
    fun `import parses daily logs correctly`() {
        val json = DataExporter.exportToJson(testProfile, testLogs, testAssessments, testStreak)
        val imported = DataImporter.parseFromJson(json)

        assertEquals(3, imported.logs.size)
        assertEquals(LocalDate.of(2025, 3, 1), imported.logs[0].date)
        assertEquals(10, imported.logs[0].rung)
        assertTrue(imported.logs[0].completed)
        assertFalse(imported.logs[2].completed)
        assertNull(imported.logs[2].completedAt)
    }

    @Test
    fun `import parses assessment logs correctly`() {
        val json = DataExporter.exportToJson(testProfile, testLogs, testAssessments, testStreak)
        val imported = DataImporter.parseFromJson(json)

        assertEquals(2, imported.assessments.size)
        assertEquals(5, imported.assessments[0].fromRung)
        assertEquals(6, imported.assessments[0].toRung)
        assertTrue(imported.assessments[0].passed)
        assertEquals("Easy", imported.assessments[0].notes)
    }

    @Test
    fun `import rejects wrong version`() {
        val json = """{"version": 2, "app": "HackerFit", "streak": {"streakCount": 0, "freezesBanked": 0, "lastFreezeEarnDate": ""}, "dailyLogs": [], "assessmentLogs": []}"""
        try {
            DataImporter.parseFromJson(json)
            fail("Should have thrown")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("nao suportada"))
        }
    }

    @Test
    fun `import rejects missing version`() {
        val json = """{"app": "HackerFit", "streak": {"streakCount": 0, "freezesBanked": 0, "lastFreezeEarnDate": ""}, "dailyLogs": [], "assessmentLogs": []}"""
        try {
            DataImporter.parseFromJson(json)
            fail("Should have thrown")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("nao suportada"))
        }
    }

    @Test
    fun `roundtrip preserves all data`() {
        val json = DataExporter.exportToJson(testProfile, testLogs, testAssessments, testStreak)
        val imported = DataImporter.parseFromJson(json)

        assertEquals(testProfile.currentRung, imported.profile!!.currentRung)
        assertEquals(testProfile.phase, imported.profile!!.phase)
        assertEquals(testProfile.rungStartDate, imported.profile!!.rungStartDate)
        assertEquals(testProfile.dailyReminderHour, imported.profile!!.dailyReminderHour)
        assertEquals(testProfile.dailyReminderMinute, imported.profile!!.dailyReminderMinute)
        assertEquals(testProfile.onboardingComplete, imported.profile!!.onboardingComplete)

        assertEquals(testStreak.streakCount, imported.streak.streakCount)
        assertEquals(testStreak.freezesBanked, imported.streak.freezesBanked)
        assertEquals(testStreak.lastFreezeEarnDate, imported.streak.lastFreezeEarnDate)

        assertEquals(testLogs.size, imported.logs.size)
        testLogs.forEachIndexed { i, expected ->
            assertEquals(expected.date, imported.logs[i].date)
            assertEquals(expected.rung, imported.logs[i].rung)
            assertEquals(expected.completed, imported.logs[i].completed)
            assertEquals(expected.completedAt, imported.logs[i].completedAt)
        }

        assertEquals(testAssessments.size, imported.assessments.size)
        testAssessments.forEachIndexed { i, expected ->
            assertEquals(expected.date, imported.assessments[i].date)
            assertEquals(expected.fromRung, imported.assessments[i].fromRung)
            assertEquals(expected.toRung, imported.assessments[i].toRung)
            assertEquals(expected.passed, imported.assessments[i].passed)
        }
    }

    @Test
    fun `import with null streak date produces null`() {
        val json = """{"version": 1, "app": "HackerFit", "profile": null, "streak": {"streakCount": 5, "freezesBanked": 1, "lastFreezeEarnDate": ""}, "dailyLogs": [], "assessmentLogs": []}"""
        val imported = DataImporter.parseFromJson(json)
        assertNull(imported.streak.lastFreezeEarnDate)
    }

    @Test
    fun `import with no profile returns null profile`() {
        val json = """{"version": 1, "app": "HackerFit", "streak": {"streakCount": 0, "freezesBanked": 0, "lastFreezeEarnDate": ""}, "dailyLogs": [], "assessmentLogs": []}"""
        val imported = DataImporter.parseFromJson(json)
        assertNull(imported.profile)
    }

    @Test
    fun `import with null reminder produces null fields`() {
        val json = """{"version": 1, "app": "HackerFit", "profile": {"currentRung": 5, "phase": "introductory", "rungStartDate": "2025-01-01", "dailyReminderHour": null, "dailyReminderMinute": null, "onboardingComplete": true}, "streak": {"streakCount": 0, "freezesBanked": 0, "lastFreezeEarnDate": ""}, "dailyLogs": [], "assessmentLogs": []}"""
        val imported = DataImporter.parseFromJson(json)
        assertNull(imported.profile!!.dailyReminderHour)
        assertNull(imported.profile!!.dailyReminderMinute)
    }
}
