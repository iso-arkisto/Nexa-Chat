package com.yourname.chat.presentation.screen.chat_screen

import com.yourname.chat.presentation.components.UiText

data class MessageInputState(
    val canSend: Boolean,
    val restrictionReason: UiText? = null
)
