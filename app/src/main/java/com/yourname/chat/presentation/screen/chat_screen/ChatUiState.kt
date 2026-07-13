package com.yourname.chat.presentation.screen.chat_screen

import com.yourname.chat.data.model.message.Message
import com.yourname.chat.data.model.user.User
import com.yourname.chat.data.model.user.UserStatus
import com.yourname.chat.presentation.components.UiText

sealed interface ChatUiState {
    data object Loading : ChatUiState

    data class Error(val message: UiText) : ChatUiState

    data class Success(

        val chatHeader: ChatHeaderState,
        val messageInput: MessageInputState,
        val dialogs: ChatDialogsState,

        val allMessages: List<Message>,
        val selectedMessages: List<Message> = emptyList(),
        val targetUser: User,
        val currentUser: User,
    ) : ChatUiState
}