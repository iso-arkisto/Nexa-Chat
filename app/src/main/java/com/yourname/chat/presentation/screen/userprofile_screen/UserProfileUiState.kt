package com.yourname.chat.presentation.screen.userprofile_screen

import com.yourname.chat.data.model.user.User
import com.yourname.chat.data.model.user.UserStatus

data class UserProfileUiState(
    val isLoading: Boolean = true,
    val targetUser: User? = null,
    val currentUser: User? = null,
    val userStatus: UserStatus? = null,
    val error: String? = null
)
