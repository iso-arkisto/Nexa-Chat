package com.yourname.chat.presentation.screen.chat_screen

import com.yourname.chat.data.model.message.Message

sealed interface ChatDialogsState {
    data object None : ChatDialogsState
    data class DeleteMessage(val messages: List<Message>) : ChatDialogsState
    data class EditMessage(val messageId: String) : ChatDialogsState
}