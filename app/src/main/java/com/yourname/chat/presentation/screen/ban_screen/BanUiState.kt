package com.yourname.chat.presentation.screen.ban_screen

import com.yourname.chat.data.model.user.BanData

sealed class BanUiState {
    object Loading: BanUiState()
    data class Success(val data: BanData?): BanUiState()
    data class Error(val message: String): BanUiState()
}