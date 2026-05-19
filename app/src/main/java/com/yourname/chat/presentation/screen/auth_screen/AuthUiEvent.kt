package com.yourname.chat.presentation.screen.auth_screen

sealed class AuthUiEvent {
    data class Success(
        val route: String
    ): AuthUiEvent()

    data class Error(
        val message: String
    ): AuthUiEvent()
}
