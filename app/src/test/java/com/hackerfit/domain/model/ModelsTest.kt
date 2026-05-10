package com.hackerfit.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ModelsTest {

    @Test
    fun `UserProfile copy changes single field`() {
        val original = UserProfile(5, Phase.INTRODUCTORY, LocalDate.now(), null, null, true)
        val copied = original.copy(currentRung = 10)
        assertEquals(10, copied.currentRung)
        assertEquals(original.phase, copied.phase)
        assertEquals(original.rungStartDate, copied.rungStartDate)
    }

    @Test
    fun `UserProfile equality works`() {
        val date = LocalDate.now()
        val p1 = UserProfile(5, Phase.INTRODUCTORY, date, null, null, true)
        val p2 = UserProfile(5, Phase.INTRODUCTORY, date, null, null, true)
        assertEquals(p1, p2)
    }

    @Test
    fun `UserProfile inequality works`() {
        val p1 = UserProfile(5, Phase.INTRODUCTORY, LocalDate.now(), null, null, true)
        val p2 = UserProfile(6, Phase.INTRODUCTORY, LocalDate.now(), null, null, true)
        assertNotEquals(p1, p2)
    }

    @Test
    fun `DailyLog default id is 0`() {
        val log = DailyLog(date = LocalDate.now(), rung = 1, completed = true)
        assertEquals(0L, log.id)
    }

    @Test
    fun `DailyLog default completedAt is null`() {
        val log = DailyLog(date = LocalDate.now(), rung = 1, completed = true)
        assertNull(log.completedAt)
    }

    @Test
    fun `DailyLog equality works`() {
        val date = LocalDate.now()
        val l1 = DailyLog(1, date, 5, true, date)
        val l2 = DailyLog(1, date, 5, true, date)
        assertEquals(l1, l2)
    }

    @Test
    fun `AssessmentLog default id is 0`() {
        val log = AssessmentLog(date = LocalDate.now(), fromRung = 1, toRung = 2, passed = true)
        assertEquals(0L, log.id)
    }

    @Test
    fun `AssessmentLog default notes is null`() {
        val log = AssessmentLog(date = LocalDate.now(), fromRung = 1, toRung = 2, passed = true)
        assertNull(log.notes)
    }

    @Test
    fun `AssessmentLog with notes`() {
        val log = AssessmentLog(date = LocalDate.now(), fromRung = 1, toRung = 2, passed = true, notes = "test")
        assertEquals("test", log.notes)
    }

    @Test
    fun `StreakData default constructor`() {
        val data = StreakData(0, 0, null)
        assertEquals(0, data.streakCount)
        assertEquals(0, data.freezesBanked)
        assertNull(data.lastFreezeEarnDate)
    }

    @Test
    fun `StreakData copy works`() {
        val original = StreakData(10, 2, LocalDate.now())
        val copied = original.copy(streakCount = 15)
        assertEquals(15, copied.streakCount)
        assertEquals(2, copied.freezesBanked)
    }

    @Test
    fun `Phase enum has two values`() {
        assertEquals(2, Phase.values().size)
        assertEquals(Phase.INTRODUCTORY, Phase.valueOf("INTRODUCTORY"))
        assertEquals(Phase.LIFETIME, Phase.valueOf("LIFETIME"))
    }

    @Test
    fun `WorkoutExercise default isRunJump is false`() {
        val ex = WorkoutExercise(0, "Test", "Desc", 10)
        assertFalse(ex.isRunJump)
    }

    @Test
    fun `WorkoutExercise default sets extraSteps jumpingJacksPerSet are 0`() {
        val ex = WorkoutExercise(0, "Test", "Desc", 10)
        assertEquals(0, ex.sets)
        assertEquals(0, ex.extraSteps)
        assertEquals(0, ex.jumpingJacksPerSet)
    }

    @Test
    fun `WorkoutExercise with all fields`() {
        val ex = WorkoutExercise(4, "Run", "Desc", 100, true, 3, 20, 10)
        assertEquals(4, ex.index)
        assertEquals("Run", ex.name)
        assertEquals(100, ex.targetReps)
        assertTrue(ex.isRunJump)
        assertEquals(3, ex.sets)
        assertEquals(20, ex.extraSteps)
        assertEquals(10, ex.jumpingJacksPerSet)
    }

    @Test
    fun `Rung data class`() {
        val rung = Rung(1, Phase.INTRODUCTORY, 2, 3, 4, 5, 1, 30, 7)
        assertEquals(1, rung.number)
        assertEquals(Phase.INTRODUCTORY, rung.phase)
        assertEquals(2, rung.bend)
        assertEquals(7, rung.jumpingJacksPerSet)
    }

    @Test
    fun `WorkoutSession data class`() {
        val rung = Rung(1, Phase.INTRODUCTORY, 2, 3, 4, 5, 1, 30, 7)
        val exercises = listOf(WorkoutExercise(0, "A", "B", 5))
        val session = WorkoutSession(rung, exercises)
        assertEquals(rung, session.rung)
        assertEquals(1, session.exercises.size)
    }
}
