package com.hackerfit.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.hackerfit.ui.theme.HackerFitTheme
import org.junit.Rule
import org.junit.Test

class RepCounterTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun repCounterShowsTarget() {
        composeTestRule.setContent {
            HackerFitTheme {
                RepCounter(currentReps = 5, targetReps = 10, onIncrement = {}, onDecrement = {})
            }
        }
        composeTestRule.onNodeWithText("Alvo: 10").assertIsDisplayed()
    }

    @Test
    fun repCounterShowsCurrentReps() {
        composeTestRule.setContent {
            HackerFitTheme {
                RepCounter(currentReps = 7, targetReps = 10, onIncrement = {}, onDecrement = {})
            }
        }
        composeTestRule.onNodeWithText("7").assertIsDisplayed()
    }

    @Test
    fun repCounterShowsGoalReachedWhenMet() {
        composeTestRule.setContent {
            HackerFitTheme {
                RepCounter(currentReps = 10, targetReps = 10, onIncrement = {}, onDecrement = {})
            }
        }
        composeTestRule.onNodeWithText("Meta batida!").assertIsDisplayed()
    }

    @Test
    fun repCounterHidesGoalWhenNotMet() {
        composeTestRule.setContent {
            HackerFitTheme {
                RepCounter(currentReps = 5, targetReps = 10, onIncrement = {}, onDecrement = {})
            }
        }
        composeTestRule.onNodeWithText("Meta batida!").assertDoesNotExist()
    }

    @Test
    fun repCounterShowsZeroReps() {
        composeTestRule.setContent {
            HackerFitTheme {
                RepCounter(currentReps = 0, targetReps = 10, onIncrement = {}, onDecrement = {})
            }
        }
        composeTestRule.onNodeWithText("0").assertIsDisplayed()
    }

    @Test
    fun repCounterIncrementButtonIsDisplayed() {
        composeTestRule.setContent {
            HackerFitTheme {
                RepCounter(currentReps = 5, targetReps = 10, onIncrement = {}, onDecrement = {})
            }
        }
        composeTestRule.onNodeWithText("+").assertIsDisplayed()
    }

    @Test
    fun repCounterDecrementButtonIsDisplayed() {
        composeTestRule.setContent {
            HackerFitTheme {
                RepCounter(currentReps = 5, targetReps = 10, onIncrement = {}, onDecrement = {})
            }
        }
        composeTestRule.onNodeWithText("-").assertIsDisplayed()
    }

    @Test
    fun repCounterGoalReachedWhenExceedingTarget() {
        composeTestRule.setContent {
            HackerFitTheme {
                RepCounter(currentReps = 15, targetReps = 10, onIncrement = {}, onDecrement = {})
            }
        }
        composeTestRule.onNodeWithText("Meta batida!").assertIsDisplayed()
    }
}
