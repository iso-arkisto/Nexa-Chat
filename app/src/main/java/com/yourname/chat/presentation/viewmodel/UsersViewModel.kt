package com.yourname.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.chat.data.model.user.BanData
import com.yourname.chat.data.model.user.PrivacySetting
import com.yourname.chat.data.model.user.User
import com.yourname.chat.data.repository.UserRepository
import com.yourname.chat.domain.CheckUserAccessUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: UserRepository,
    private val checkUserAccessUseCase: CheckUserAccessUseCase
): ViewModel() {
    private val _navigationEvent = Channel<BanData>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            repository.getCurrentUserData().collect { user ->
                _currentUser.value = user

                if(user != null) {
                    if(user.moderation.banInfo.banned) {
                        _navigationEvent.send(user.moderation.banInfo)
                    }
                }
            }
        }
    }

    fun checkAccess(user1: User, perm: String, user2: User): Boolean {
        return checkUserAccessUseCase.invoke(user1, user2, perm)
    }

    fun logOut() {
        viewModelScope.launch {
            repository.logOut()
        }
    }
}