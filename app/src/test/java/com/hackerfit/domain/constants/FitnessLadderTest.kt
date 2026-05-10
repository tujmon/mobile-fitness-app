package com.hackerfit.domain.constants

import org.junit.Assert.*
import org.junit.Test

class FitnessLadderTest {

    @Test
    fun `introductory ladder has 15 rungs`() {
        assertEquals(15, FitnessLadder.introductoryLadder.size)
    }

    @Test
    fun `lifetime ladder has 33 rungs`() {
        assertEquals(33, FitnessLadder.lifetimeLadder.size)
    }

    @Test
    fun `total rungs is 48`() {
        assertEquals(48, FitnessLadder.introductoryLadder.size + FitnessLadder.lifetimeLadder.size)
    }

    @Test
    fun `exercises list has 5 items`() {
        assertEquals(5, FitnessLadder.exercises.size)
    }

    @Test
    fun `exercise indices are sequential 0 to 4`() {
        val indices = FitnessLadder.exercises.map { it.index }
        assertEquals(listOf(0, 1, 2, 3, 4), indices)
    }

    @Test
    fun `exercise names are correct`() {
        assertEquals("Flexao para Frente", FitnessLadder.exercises[0].name)
        assertEquals("Abdominal", FitnessLadder.exercises[1].name)
        assertEquals("Elevacao de Pernas", FitnessLadder.exercises[2].name)
        assertEquals("Flexao de Braco", FitnessLadder.exercises[3].name)
        assertEquals("Corrida e Salto", FitnessLadder.exercises[4].name)
    }

    @Test
    fun `each exercise has non-empty descriptions`() {
        FitnessLadder.exercises.forEach { ex ->
            assertTrue(ex.introductoryDescription.isNotEmpty())
            assertTrue(ex.lifetimeDescription.isNotEmpty())
        }
    }

    @Test
    fun `getRung returns first introductory rung`() {
        val rung = FitnessLadder.getRung(1)
        assertEquals(1, rung.number)
        assertEquals(2, rung.bend)
        assertEquals(3, rung.sitUp)
        assertEquals(4, rung.legLift)
        assertEquals(2, rung.pushUp)
        assertEquals(1, rung.runJumpSets)
        assertEquals(30, rung.runJumpExtraSteps)
    }

    @Test
    fun `getRung returns last introductory rung`() {
        val rung = FitnessLadder.getRung(15)
        assertEquals(15, rung.number)
        assertEquals(28, rung.bend)
        assertEquals(25, rung.sitUp)
    }

    @Test
    fun `getRung returns first lifetime rung`() {
        val rung = FitnessLadder.getRung(16)
        assertEquals(16, rung.number)
        assertEquals(14, rung.bend)
    }

    @Test
    fun `getRung returns last lifetime rung`() {
        val rung = FitnessLadder.getRung(48)
        assertEquals(48, rung.number)
        assertEquals(80, rung.bend)
        assertEquals(69, rung.sitUp)
    }

    @Test
    fun `getRung returns middle rung`() {
        val rung = FitnessLadder.getRung(25)
        assertEquals(25, rung.number)
        assertEquals(27, rung.bend)
    }

    @Test
    fun `isIntroductory is true for rungs 1-15`() {
        for (i in 1..15) {
            assertTrue("Rung $i should be introductory", FitnessLadder.isIntroductory(i))
        }
    }

    @Test
    fun `isIntroductory is false for rungs 16-48`() {
        for (i in 16..48) {
            assertFalse("Rung $i should not be introductory", FitnessLadder.isIntroductory(i))
        }
    }

    @Test
    fun `getPhase returns introductory for rungs 1-15`() {
        assertEquals("introductory", FitnessLadder.getPhase(1))
        assertEquals("introductory", FitnessLadder.getPhase(15))
    }

    @Test
    fun `getPhase returns lifetime for rungs 16-48`() {
        assertEquals("lifetime", FitnessLadder.getPhase(16))
        assertEquals("lifetime", FitnessLadder.getPhase(48))
    }

    @Test
    fun `getJumpingJacksPerSet returns 7 for introductory`() {
        assertEquals(7, FitnessLadder.getJumpingJacksPerSet(1))
        assertEquals(7, FitnessLadder.getJumpingJacksPerSet(15))
    }

    @Test
    fun `getJumpingJacksPerSet returns 10 for lifetime`() {
        assertEquals(10, FitnessLadder.getJumpingJacksPerSet(16))
        assertEquals(10, FitnessLadder.getJumpingJacksPerSet(48))
    }

    @Test
    fun `introductory rung numbers are sequential 1-15`() {
        for (i in FitnessLadder.introductoryLadder.indices) {
            assertEquals(i + 1, FitnessLadder.introductoryLadder[i].number)
        }
    }

    @Test
    fun `lifetime rung numbers are sequential 16-48`() {
        for (i in FitnessLadder.lifetimeLadder.indices) {
            assertEquals(i + 16, FitnessLadder.lifetimeLadder[i].number)
        }
    }

    @Test
    fun `all rung data has positive or zero values`() {
        val allRungs = FitnessLadder.introductoryLadder + FitnessLadder.lifetimeLadder
        allRungs.forEach { rung ->
            assertTrue("bend for rung ${rung.number}", rung.bend > 0)
            assertTrue("sitUp for rung ${rung.number}", rung.sitUp > 0)
            assertTrue("legLift for rung ${rung.number}", rung.legLift > 0)
            assertTrue("pushUp for rung ${rung.number}", rung.pushUp > 0)
            assertTrue("runJumpSets for rung ${rung.number}", rung.runJumpSets > 0)
            assertTrue("runJumpExtraSteps for rung ${rung.number}", rung.runJumpExtraSteps >= 0)
        }
    }

    @Test
    fun `difficulty generally increases across introductory ladder`() {
        val bends = FitnessLadder.introductoryLadder.map { it.bend }
        for (i in 1 until bends.size) {
            assertTrue(
                "Rung ${i + 1} bend (${bends[i]}) should be >= rung $i bend (${bends[i - 1]})",
                bends[i] >= bends[i - 1]
            )
        }
    }
}
