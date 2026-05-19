package com.yourname.chat.presentation.screen.ban_screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.yourname.chat.data.model.user.BanData
import com.yourname.chat.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BanViewModel @Inject constructor(
    private val repository: UserRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiEvent = Channel<BanUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<BanUiState> = repository.observeBanInfo(userId)
        .map { data ->
            if(data == null) BanUiState.Error("Unexpected error") else BanUiState.Success(data)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BanUiState.Loading
        )

    fun changeUserStatus(data: Map<String, Any?>) {
        viewModelScope.launch {
            repository.updateCurrentUserStatus(data)
        }
    }

    fun unbanUser() {
       viewModelScope.launch {
           repository.unbanUser()
               .onSuccess {
                   sendUiEvent(BanUiEvent.ShowToast("Successfully unbanned"))
               }
               .onFailure { error ->
                   sendUiEvent(BanUiEvent.ShowToast("Unban failed. Error: ${error.message}"))
               }
       }
    }

    private fun sendUiEvent(event: BanUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}