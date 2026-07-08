package com.yourname.chat.presentation.screen.chat_screen

sealed interface ChatDialogsState {
    data object None : ChatDialogsState
    data class DeleteMessage(val messageId: String) : ChatDialogsState
    data class EditMessage(val messageId: String) : ChatDialogsState
}