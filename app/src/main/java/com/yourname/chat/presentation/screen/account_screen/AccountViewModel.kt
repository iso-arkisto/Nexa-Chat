package com.yourname.chat.presentation.screen.account_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.chat.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Any
import kotlin.String

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: UserRepository,
): ViewModel() {
    private val _uiEvent = Channel<AccountUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val userState = repository.getCurrentUserData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateAccountData(map: HashMap<String,Any?>) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.updateCurrentUserData(map)
                .onSuccess {
                    sendUiEvent(AccountUiEvent.ShowToast("Saved successfully"))
                }
                .onFailure { error ->
                    sendUiEvent(AccountUiEvent.ShowToast("Error: ${error.message}"))
                }
            _isLoading.value = false
        }
    }

    suspend fun checkUsernameAvailability(username: String): Boolean {
        if(username.isBlank()) {
            return true
        }

        val result = repository.checkUsernameAvailability(username)

        return result.fold(
            onSuccess = { it },
            onFailure = { error ->
                sendUiEvent(AccountUiEvent.ShowToast("Error: ${error.message}"))
                false
            }
        )
    }

    private fun sendUiEvent(event: AccountUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}