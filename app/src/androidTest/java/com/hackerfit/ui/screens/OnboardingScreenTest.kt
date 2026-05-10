package com.hackerfit.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.hackerfit.ui.screens.onboarding.OnboardingScreen
import com.hackerfit.ui.theme.HackerFitTheme
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onboardingShowsWelcomeTitle() {
        composeTestRule.setContent {
            HackerFitTheme { OnboardingScreen(onComplete = {}) }
        }
        composeTestRule.onNodeWithText("Bem-vindo ao HackerFit").assertIsDisplayed()
    }

    @Test
    fun onboardingShowsNextButton() {
        composeTestRule.setContent {
            HackerFitTheme { OnboardingScreen(onComplete = {}) }
        }
        composeTestRule.onNodeWithText("Pr\u00f3ximo").assertIsDisplayed()
    }

    @Test
    fun onboardingShowsSkipButton() {
        composeTestRule.setContent {
            HackerFitTheme { OnboardingScreen(onComplete = {}) }
        }
        composeTestRule.onNodeWithText("Pular").assertIsDisplayed()
    }

    @Test
    fun onboardingShowsSubtitle() {
        composeTestRule.setContent {
            HackerFitTheme { OnboardingScreen(onComplete = {}) }
        }
        composeTestRule.onNodeWithText("Exercicio para viver mais e melhor").assertIsDisplayed()
    }

    @Test
    fun onboardingClickingNextAdvancesToPage2() {
        composeTestRule.setContent {
            HackerFitTheme { OnboardingScreen(onComplete = {}) }
        }
        composeTestRule.onNodeWithText("Pr\u00f3ximo").performClick()
        composeTestRule.onNodeWithText("A Escada Fitness").assertIsDisplayed()
    }

    @Test
    fun onboardingLastPageShowsStartButton() {
        composeTestRule.setContent {
            HackerFitTheme { OnboardingScreen(onComplete = {}) }
        }
        repeat(3) {
            composeTestRule.onNodeWithText("Pr\u00f3ximo").performClick()
        }
        composeTestRule.onNodeWithText("Come\u00e7ar").assertIsDisplayed()
    }

    @Test
    fun onboardingSkipButtonDisappearsOnLastPage() {
        composeTestRule.setContent {
            HackerFitTheme { OnboardingScreen(onComplete = {}) }
        }
        repeat(3) {
            composeTestRule.onNodeWithText("Pr\u00f3ximo").performClick()
        }
        composeTestRule.onNodeWithText("Pular").assertDoesNotExist()
    }
}
