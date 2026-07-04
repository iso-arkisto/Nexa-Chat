package com.yourname.chat.presentation.screen.userprofile_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.chat.domain.CheckUserAccessUseCase
import com.yourname.chat.domain.repository.UserRepository
import com.yourname.chat.presentation.components.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: UserRepository,
    private val checkAccessUseCase: CheckUserAccessUseCase
): ViewModel() {
    val userId: String? = savedStateHandle["userId"]

    private val _uiEvent = Channel<UserProfileUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    val uiState: StateFlow<UserProfileUiState> = retryTrigger
        .flatMapLatest {

            if(userId.isNullOrBlank()) {
                return@flatMapLatest flowOf(
                    UserProfileUiState.Error(UiText.DynamicString("User ID is missing"))
                )
            }

            combine(
                repository.getUserData(userId),
                repository.getCurrentUserData(),
                repository.getUserStatus(userId)
            ) { target, current, status ->
                if(target != null && current != null && status != null) {
                    UserProfileUiState.Success(
                        targetUser = target,
                        currentUser = current,
                        userStatus = status,
                        checkAccess = checkAccessUseCase::invoke
                    )
                } else {
                    UserProfileUiState.Loading
                }
            }
                .catch { e ->
                    emit(UserProfileUiState.Error(UiText.DynamicString(e.localizedMessage ?: "Unknown error")))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileUiState.Loading
        )

    fun retry() {
       retryTrigger.tryEmit(Unit)
    }

    fun addFriend() {
        viewModelScope.launch {
            val currentState = uiState.value

            if(currentState is UserProfileUiState.Success) {

                val currentUser = currentState.currentUser
                val targetUser = currentState.targetUser

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
    }

    private fun sendUiEvent(event: UserProfileUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}