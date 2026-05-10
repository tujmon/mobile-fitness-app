package com.hackerfit.ui.screens.ladder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackerfit.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LadderViewModel @Inject constructor(
    userProfileRepository: UserProfileRepository
) : ViewModel() {

    val currentRung: StateFlow<Int> = userProfileRepository.getProfile()
        .map { it?.currentRung ?: 1 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
}
