package com.yourname.chat.presentation.screen.userprofile_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.chat.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: UserRepository
): ViewModel() {
    val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiEvent = Channel<UserProfileUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<UserProfileUiState> = combine(
        repository.getUserData(userId),
        repository.getCurrentUserData(),
        repository.getUserStatus(userId)
    ) { targetUser, currentUser, userStatus ->
        UserProfileUiState(
            isLoading = false,
            targetUser = targetUser,
            currentUser = currentUser,
            userStatus = userStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfileUiState(isLoading = true)
    )

    fun addFriend() {
        viewModelScope.launch {
            val state = uiState.value
            val currentUser = state.currentUser
            val targetUser = state.targetUser

            if(currentUser == null || targetUser == null) {
                sendUiEvent(UserProfileUiEvent.ShowToast("User data not available"))
                return@launch
            }

            if(currentUser.core.uid == targetUser.core.uid) {
                sendUiEvent(UserProfileUiEvent.ShowToast("You can't be your own friend"))
                return@launch
            }

            repository.addFriend(currentUser, targetUser).onFailure { exception ->
                sendUiEvent(UserProfileUiEvent.ShowToast("Failed: ${exception.message}"))
            }
        }
    }

    private fun sendUiEvent(event: UserProfileUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}