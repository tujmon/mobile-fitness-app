package com.hackerfit.baselineprofile

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
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
            device.findObject(By.desc("Hist\u00f3rico"))?.click()
            device.waitForIdle()
        }
    }
}
