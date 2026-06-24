package com.yourname.chat.presentation.screen.userprofile_screen

sealed class UserProfileUiEvent {
    data class ShowToast(
        val text: String
    ): UserProfileUiEvent()
}