package com.yourname.chat.presentation.screen.chat_screen

import com.yourname.chat.presentation.components.UiText

data class ChatHeaderState(
    val title: UiText,
    val avatar: String,
    val status: UiText,
    val canEditMessage: Boolean
)
