package com.yourname.chat.presentation.screen.chatlist_screen

import androidx.compose.ui.graphics.Color
import com.yourname.chat.ui.theme.PrimaryColor

data class UserChatUiState(
    val id: String,
    val displayName: String,
    val lastMessageText: String?,
    val avatarLabel: String,
    val isOnline: Boolean,
    val isStorage: Boolean,
    val avatarColor: Color = PrimaryColor
)