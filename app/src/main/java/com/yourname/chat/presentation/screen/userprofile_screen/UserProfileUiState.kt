package com.yourname.chat.presentation.screen.userprofile_screen

import com.yourname.chat.data.model.user.User
import com.yourname.chat.data.model.user.UserStatus
import com.yourname.chat.presentation.components.UiText

sealed interface UserProfileUiState {
    data object Loading: UserProfileUiState

    data class Error(val message: UiText): UserProfileUiState

    data class Success(
        val targetUser: User,
        val currentUser: User,
        val userStatus: UserStatus
    ): UserProfileUiState
}