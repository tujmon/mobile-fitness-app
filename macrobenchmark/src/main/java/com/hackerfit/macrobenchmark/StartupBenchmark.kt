package com.hackerfit.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupCold() = rule.measureRepeated(
        packageName = "com.hackerfit",
        metrics = listOf(StartupTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.COLD,
        compilationMode = CompilationMode.Full()
    ) {
        pressHome()
        startActivityAndWait()
    }

    @Test
    fun startupWarm() = rule.measureRepeated(
        packageName = "com.hackerfit",
        metrics = listOf(StartupTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.WARM,
        compilationMode = CompilationMode.Full()
    ) {
        pressHome()
        startActivityAndWait()
    }

    @Test
    fun startupHot() = rule.measureRepeated(
        packageName = "com.hackerfit",
        metrics = listOf(StartupTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.HOT,
        compilationMode = CompilationMode.Full()
    ) {
        pressHome()
        startActivityAndWait()
    }
}
