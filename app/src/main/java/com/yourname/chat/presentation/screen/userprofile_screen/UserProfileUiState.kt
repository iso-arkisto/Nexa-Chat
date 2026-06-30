package com.yourname.chat.presentation.screen.userprofile_screen

import com.yourname.chat.data.model.user.User
import com.yourname.chat.data.model.user.UserStatus

sealed interface UserProfileUiState {
    data object Loading: UserProfileUiState

    data class Error(val message: String): UserProfileUiState

    data class Success(
        val targetUser: User,
        val currentUser: User,
        val userStatus: UserStatus
    ): UserProfileUiState
}