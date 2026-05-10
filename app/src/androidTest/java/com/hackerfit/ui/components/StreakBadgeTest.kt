package com.hackerfit.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.hackerfit.ui.theme.HackerFitTheme
import org.junit.Rule
import org.junit.Test

class StreakBadgeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun streakBadgeShowsCount() {
        composeTestRule.setContent {
            HackerFitTheme { StreakBadge(streakCount = 5, freezesBanked = 0) }
        }
        composeTestRule.onNodeWithText("5 dias").assertIsDisplayed()
    }

    @Test
    fun streakBadgeShowsZeroDays() {
        composeTestRule.setContent {
            HackerFitTheme { StreakBadge(streakCount = 0, freezesBanked = 0) }
        }
        composeTestRule.onNodeWithText("0 dias").assertIsDisplayed()
    }

    @Test
    fun streakBadgeShowsHighStreak() {
        composeTestRule.setContent {
            HackerFitTheme { StreakBadge(streakCount = 30, freezesBanked = 2) }
        }
        composeTestRule.onNodeWithText("30 dias").assertIsDisplayed()
    }

    @Test
    fun streakBadgeShowsFreezesWhenBanked() {
        composeTestRule.setContent {
            HackerFitTheme { StreakBadge(streakCount = 10, freezesBanked = 3) }
        }
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
    }

    @Test
    fun streakBadgeHidesFreezesWhenZero() {
        composeTestRule.setContent {
            HackerFitTheme { StreakBadge(streakCount = 5, freezesBanked = 0) }
        }
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }
}
