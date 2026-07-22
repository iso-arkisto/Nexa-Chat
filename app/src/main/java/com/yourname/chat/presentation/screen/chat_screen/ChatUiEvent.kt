package com.yourname.chat.presentation.screen.chat_screen

import com.yourname.chat.presentation.components.UiText

sealed class ChatUiEvent {
    data class ShowToast(
        val text: UiText
    ) : ChatUiEvent()

    data class CopyToClipboard(
        val text: String
    ) : ChatUiEvent()
}