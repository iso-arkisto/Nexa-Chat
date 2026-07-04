package com.yourname.chat.presentation.screen.userprofile_screen

import androidx.compose.ui.graphics.vector.ImageVector

data class FriendButtonState(
    val isVisible: Boolean,
    val icon: ImageVector,
    val textResId: Int
)