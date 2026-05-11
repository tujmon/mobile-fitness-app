package com.hackerfit.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hackerfit.domain.constants.FitnessLadder
import com.hackerfit.ui.screens.assessment.AssessmentScreen
import com.hackerfit.ui.screens.exercise.ExerciseDetailScreen
import com.hackerfit.ui.screens.history.HistoryScreen
import com.hackerfit.ui.screens.home.HomeScreen
import com.hackerfit.ui.screens.ladder.LadderScreen
import com.hackerfit.ui.screens.onboarding.OnboardingScreen
import com.hackerfit.ui.screens.settings.SettingsScreen
import com.hackerfit.ui.screens.stats.StatsScreen
import com.hackerfit.ui.screens.workout.WorkoutScreen

import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String,
    onOnboardingComplete: suspend () -> Unit = {},
    mainViewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    scope.launch {
                        onOnboardingComplete()
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onStartWorkout = { navController.navigate("workout") },
                onStartAssessment = { navController.navigate("assessment") },
                onViewLadder = { navController.navigate("ladder") },
                innerPadding = innerPadding
            )
        }
        composable("workout") {
            WorkoutScreen(
                onFinish = {
                    navController.popBackStack()
                },
                onExerciseInfo = { exerciseIndex ->
                    navController.navigate("exercise/$exerciseIndex")
                }
            )
        }
        composable("assessment") {
            AssessmentScreen(
                onFinish = {
                    navController.popBackStack()
                }
            )
        }
        composable("exercise/{index}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull()
                ?.coerceIn(0, FitnessLadder.exercises.lastIndex) ?: 0
            ExerciseDetailScreen(
                exerciseIndex = index,
                onBack = { navController.popBackStack() }
            )
        }
        composable("stats") {
            StatsScreen(innerPadding = innerPadding)
        }
        composable("ladder") {
            LadderScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryScreen(innerPadding = innerPadding)
        }
        composable("settings") {
            SettingsScreen(
                innerPadding = innerPadding,
                mainViewModel = mainViewModel
            )
        }
    }
}
