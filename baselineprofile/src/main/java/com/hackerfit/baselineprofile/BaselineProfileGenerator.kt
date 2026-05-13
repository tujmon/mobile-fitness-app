package com.hackerfit.baselineprofile

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @OptIn(ExperimentalBaselineProfilesApi::class)
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() {
        rule.collect(
            packageName = "com.hackerfit",
            maxIterations = 15,
            stableIterations = 3
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    @Test
    fun startupAndScrollHistory() {
        rule.collect(
            packageName = "com.hackerfit",
            maxIterations = 15,
            stableIterations = 3
        ) {
            pressHome()
            startActivityAndWait()
            val history = device.wait(Until.findObject(By.desc("Hist\u00f3rico")), 5_000)
            assertNotNull("Hist\u00f3rico n\u00e3o encontrado", history)
            history.click()
            device.waitForIdle()
        }
    }
}
