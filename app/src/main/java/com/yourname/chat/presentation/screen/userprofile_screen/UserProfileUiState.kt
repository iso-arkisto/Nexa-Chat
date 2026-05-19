package com.yourname.chat.presentation.screen.userprofile_screen

sealed class UserProfileUiState {
    data class ShowToast(
        val text: String
    ): UserProfileUiState()
}