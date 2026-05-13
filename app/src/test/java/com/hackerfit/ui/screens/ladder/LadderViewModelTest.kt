package com.hackerfit.ui.screens.ladder

import com.hackerfit.FakeUserProfileRepository
import com.hackerfit.domain.model.Phase
import com.hackerfit.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class LadderViewModelTest {

    private lateinit var profileRepo: FakeUserProfileRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        profileRepo = FakeUserProfileRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `defaults to rung 1 when no profile`() = runTest(testDispatcher) {
        profileRepo.setProfile(null)
        val viewModel = LadderViewModel(profileRepo)
        assertEquals(1, viewModel.currentRung.first())
    }

    @Test
    fun `shows current rung from profile`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(15, Phase.INTRODUCTORY, LocalDate.now(), null, null, true))
        val viewModel = LadderViewModel(profileRepo)
        assertEquals(15, viewModel.currentRung.first { it != 1 })
    }

    @Test
    fun `updates when profile rung changes`() = runTest(testDispatcher) {
        val profile = UserProfile(5, Phase.INTRODUCTORY, LocalDate.now(), null, null, true)
        profileRepo.setProfile(profile)
        val viewModel = LadderViewModel(profileRepo)
        assertEquals(5, viewModel.currentRung.first { it != 1 })

        profileRepo.updateRung(10)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(10, viewModel.currentRung.first { it == 10 })
    }

    @Test
    fun `handles lifetime phase rung`() = runTest(testDispatcher) {
        profileRepo.setProfile(UserProfile(30, Phase.LIFETIME, LocalDate.now(), null, null, true))
        val viewModel = LadderViewModel(profileRepo)
        assertEquals(30, viewModel.currentRung.first { it != 1 })
    }
}
