package com.yourname.chat.presentation.screen.chatlist_screen

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.yourname.chat.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.chat.data.model.message.Message
import com.yourname.chat.data.repository.MessageRepository
import com.yourname.chat.data.repository.UserRepository
import com.yourname.chat.ui.theme.PrimaryColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository
): ViewModel() {
    private val lastMessageCache = mutableMapOf<String, StateFlow<Message?>>()

    val allUsers = userRepository.getAllUsersFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentUser = userRepository.getCurrentUserData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val connectedUsers: StateFlow<List<UserChatUiState>> = userRepository.getConnectedUsers()
        .flatMapLatest { uidList ->
            if(uidList.isEmpty()) return@flatMapLatest flowOf(emptyList())

            val userFlows = uidList.map { uid ->
                combine(
                    userRepository.getUserData(uid),
                    userRepository.getUserStatus(uid),
                    getDecryptedLastMessage(uid)
                ) {userData, status, lastMessage ->
                    val isStorage = uid == currentUser?.value?.core?.uid
                    UserChatUiState(
                        displayName = if(!isStorage) userData?.core?.displayName ?: "User" else "My Storage",
                        avatarLabel = if(!isStorage) userData?.core?.displayName?.firstOrNull()?.toString() ?: "U" else "🔒",
                        id = uid,
                        isStorage = isStorage,
                        isOnline = status?.state == "online",
                        lastMessageText = lastMessage?.replace("\n", " ")?.replace("\r", " "),
                        avatarColor = if(isStorage) Color.Blue else PrimaryColor
                    )
                }
            }

            combine(userFlows) { it.toList() }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun getLastMessage(chatId: String): StateFlow<Message?> {
        return lastMessageCache.getOrPut(chatId) {
            messageRepository.getLastMessage(chatId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = null
                )
        }
    }

    fun getDecryptedLastMessage(chatId: String): StateFlow<String?> {
        return getLastMessage(chatId).flatMapLatest { message ->
            if(message == null) {
                flowOf(null)
            } else {
                flow<String?> {
                    emit(decryptMessage(message))
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    suspend fun decryptMessage(message: Message): String {
        return messageRepository.decryptMessage(message)
            .getOrElse {
                ""
            }
    }

}