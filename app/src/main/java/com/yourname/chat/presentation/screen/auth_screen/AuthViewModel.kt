package com.yourname.chat.presentation.screen.auth_screen

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.chat.data.remote.UserPresenceManager
import com.yourname.chat.domain.repository.AuthRepository
import com.yourname.chat.domain.repository.UserRepository
import com.yourname.chat.utils.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val userRepository: UserRepository,
    private val userPresence: UserPresenceManager
): ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = Channel<AuthUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun signIn(
        email: String,
        pass: String
    ) {
        validateCredentials(email, pass)?.let { message ->
            sendUiEvent(AuthUiEvent.Error(message))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            repository.signIn(email, pass)
                .onSuccess {
                    userRepository.getCurrentUser()
                        .onSuccess { user ->
                            if(user != null) {
                                if(user.moderation.banInfo.banned) {
                                    sendUiEvent(AuthUiEvent.Success("${Screen.Ban}/${user.core.uid}"))
                                } else {
                                    userPresence.startTracking()
                                    sendUiEvent(AuthUiEvent.Success(Screen.ChatList))
                                }
                            }
                        }
                        .onFailure { error ->
                            sendUiEvent(AuthUiEvent.Error(error.message ?: "Unexpected error"))
                        }
                }
                .onFailure { error ->
                    sendUiEvent(AuthUiEvent.Error(error.message ?: "Unexpected error"))
                }
            _isLoading.value = false
        }
    }

    fun signUp(
        email: String,
        pass: String
    ) {
        validateCredentials(email, pass)?.let { message ->
            sendUiEvent(AuthUiEvent.Error(message))
            return
        }

       viewModelScope.launch {
           _isLoading.value = true
           repository.signUp(email, pass)
               .onSuccess { user ->
                   userPresence.startTracking()
                   sendUiEvent(AuthUiEvent.Success(Screen.ChatList))
               }
               .onFailure { error ->
                   sendUiEvent(AuthUiEvent.Error(error.message ?: "Unexpected error"))
               }
           _isLoading.value = false
       }
    }

    private fun validateCredentials(email: String, pass: String): String? {
        val error = isValidData(email, pass)
        return when (error) {
            ValidationError.EmptyEmail -> "Email cannot be empty"
            ValidationError.InvalidEmail -> "Invalid email format"
            ValidationError.EmptyPassword -> "Password cannot be empty"
            ValidationError.PasswordTooShort -> "Password is too short"
            else -> null
        }
    }

    private fun isValidData(email: String, pass: String): ValidationError? {
        return when {
            email.isBlank() -> ValidationError.EmptyEmail
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationError.InvalidEmail
            pass.isBlank() -> ValidationError.EmptyPassword
            pass.length < 6 -> ValidationError.PasswordTooShort
            else -> null
        }
    }

    private fun sendUiEvent(event: AuthUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}