package com.yourname.chat.presentation.screen.chat_screen

sealed class ChatUiEvent {
    data class ShowToast(
        val text: String
    ): ChatUiEvent()
}