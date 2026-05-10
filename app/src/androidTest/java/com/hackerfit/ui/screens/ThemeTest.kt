package com.hackerfit.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.hackerfit.ui.theme.HackerFitTheme
import org.junit.Rule
import org.junit.Test

class ThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun themeRendersContentInLightMode() {
        composeTestRule.setContent {
            HackerFitTheme(darkTheme = false) {
                androidx.compose.material3.Text("Light Theme Test")
            }
        }
        composeTestRule.onNodeWithText("Light Theme Test").assertIsDisplayed()
    }

    @Test
    fun themeRendersContentInDarkMode() {
        composeTestRule.setContent {
            HackerFitTheme(darkTheme = true) {
                androidx.compose.material3.Text("Dark Theme Test")
            }
        }
        composeTestRule.onNodeWithText("Dark Theme Test").assertIsDisplayed()
    }
}
