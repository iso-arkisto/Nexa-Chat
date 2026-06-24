package com.yourname.chat.presentation.screen.userprofile_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.chat.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
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

    val userState = repository.getUserData(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val currentUser = repository.getCurrentUserData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    val userStatus = repository.getUserStatus(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun addFriend() {
        viewModelScope.launch {

            val currentUser = repository.getCurrentUserData().first()
            val targetUser = repository.getUserData(userId).first()

            if(currentUser == null || targetUser == null) {
                sendUiEvent(UserProfileUiEvent.ShowToast("User data not available"))
                return@launch
            }

            if(currentUser.core.uid == targetUser.core.uid) {
                sendUiEvent(UserProfileUiEvent.ShowToast("You can't be your own friend"))
                return@launch
            }

            repository.addFriend(currentUser, targetUser).onFailure { exception ->
                sendUiEvent(UserProfileUiEvent.ShowToast("Failed to interact with Friend button. Error: ${exception.message}"))
            }
        }
    }

    private fun sendUiEvent(event: UserProfileUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}