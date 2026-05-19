package com.yourname.chat.presentation.screen.auth_screen

sealed class ValidationError {
    object EmptyEmail : ValidationError()
    object InvalidEmail : ValidationError()
    object EmptyPassword : ValidationError()
    object PasswordTooShort : ValidationError()
}