package com.hackerfit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hackerfit.domain.model.Phase
import com.hackerfit.domain.model.UserProfile
import com.hackerfit.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("home", "Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("stats", "Estat\u00edsticas", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem("history", "Hist\u00f3rico", Icons.Filled.History, Icons.Outlined.History),
    BottomNavItem("settings", "Config", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@HiltViewModel
class MainViewModel @Inject constructor(
    val userProfileRepository: UserProfileRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val KEY_PENDING_IMPORT_URI = "pending_import_uri"
    }

    val pendingImportUri = savedStateHandle.getStateFlow<String?>(KEY_PENDING_IMPORT_URI, null)

    fun setPendingImportUri(uri: String) {
        savedStateHandle[KEY_PENDING_IMPORT_URI] = uri
    }

    fun clearPendingImportUri() {
        savedStateHandle[KEY_PENDING_IMPORT_URI] = null
    }

    init {
        viewModelScope.launch {
            val existing = userProfileRepository.getProfile().first()
            if (existing == null) {
                userProfileRepository.saveProfile(
                    UserProfile(
                        currentRung = 1,
                        phase = Phase.INTRODUCTORY,
                        rungStartDate = java.time.LocalDate.now(),
                        dailyReminderHour = null,
                        dailyReminderMinute = null,
                        onboardingComplete = false
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackerFitNavHost(
    mainViewModel: MainViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val scope = rememberCoroutineScope()

    val profile by mainViewModel.userProfileRepository.getProfile().collectAsState(initial = null)
    val startDestination = remember(profile) {
        val p = profile
        when {
            p == null -> null
            p.onboardingComplete -> "home"
            else -> "onboarding"
        }
    }

    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    LaunchedEffect(Unit) {
        combine(
            mainViewModel.pendingImportUri.filterNotNull(),
            mainViewModel.userProfileRepository.getProfile().filterNotNull()
        ) { uri, _ -> uri }
            .collect {
                mainViewModel.clearPendingImportUri()
                navController.navigate("settings") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
    }

    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            innerPadding = innerPadding,
            startDestination = startDestination,
            onOnboardingComplete = {
                mainViewModel.userProfileRepository.completeOnboarding()
            },
            mainViewModel = mainViewModel
        )
    }
}
