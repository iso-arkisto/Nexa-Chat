package com.yourname.chat.presentation.screen.account_screen

sealed class AccountUiEvent {
    data class ShowToast(
        val text: String
    ): AccountUiEvent()
}