package com.hackerfit.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.hackerfit.domain.model.WorkoutExercise
import com.hackerfit.ui.theme.HackerFitTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ExerciseCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testExercise = WorkoutExercise(
        index = 1,
        name = "Abdominal",
        description = "Test",
        targetReps = 10
    )

    @Test
    fun exerciseCardShowsName() {
        composeTestRule.setContent {
            HackerFitTheme { ExerciseCard(exercise = testExercise, targetReps = 10) }
        }
        composeTestRule.onNodeWithText("Abdominal").assertIsDisplayed()
    }

    @Test
    fun exerciseCardShowsRepsForNonRunJump() {
        composeTestRule.setContent {
            HackerFitTheme { ExerciseCard(exercise = testExercise, targetReps = 10) }
        }
        composeTestRule.onNodeWithText("10 repeti\u00e7\u00f5es").assertIsDisplayed()
    }

    @Test
    fun exerciseCardShowsSetsForRunJump() {
        val runJump = WorkoutExercise(
            index = 4,
            name = "Corrida e Salto",
            description = "Test",
            targetReps = 100,
            isRunJump = true,
            sets = 3,
            extraSteps = 20,
            jumpingJacksPerSet = 10
        )
        composeTestRule.setContent {
            HackerFitTheme { ExerciseCard(exercise = runJump, targetReps = 3) }
        }
        composeTestRule.onNodeWithText("Corrida e Salto").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 sets + 20 passos").assertIsDisplayed()
    }

    @Test
    fun exerciseCardShowsDifferentRepCounts() {
        composeTestRule.setContent {
            HackerFitTheme { ExerciseCard(exercise = testExercise, targetReps = 25) }
        }
        composeTestRule.onNodeWithText("25 repeti\u00e7\u00f5es").assertIsDisplayed()
    }
}
