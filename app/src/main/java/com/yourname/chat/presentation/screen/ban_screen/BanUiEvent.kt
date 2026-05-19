package com.yourname.chat.presentation.screen.ban_screen

sealed class BanUiEvent {
    data class ShowToast(
        val text: String
    ): BanUiEvent()
}
