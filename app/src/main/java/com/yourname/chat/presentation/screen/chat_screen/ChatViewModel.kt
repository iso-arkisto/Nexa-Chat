package com.yourname.chat.presentation.screen.chat_screen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.chat.data.local.MessageEntity
import com.yourname.chat.data.model.message.Message
import com.yourname.chat.domain.repository.MessageRepository
import com.yourname.chat.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
): ViewModel() {
    val chatId: String = checkNotNull(savedStateHandle["chatId"])

    private val _uiEvent = Channel<ChatUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _canSend = MutableStateFlow(true)
    val canSend: StateFlow<Boolean> = _canSend.asStateFlow()

    private val _allMessages = MutableStateFlow<List<Message>>(emptyList())
    val allMessages: StateFlow<List<Message>> = _allMessages.asStateFlow()

    val otherUserState = userRepository.getUserData(chatId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val currentUserState = userRepository.getCurrentUserData()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val otherUserStatus =  userRepository.getUserStatus(chatId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            messageRepository.getMessages(chatId)
                .collect { messages ->
                val decryptedMessages = messages.map { msg ->
                    msg.copy(text = decryptMessage(msg))
                }
                _allMessages.value = decryptedMessages
            }
        }
    }

    private suspend fun decryptMessage(message: Message): String {
        return messageRepository.decryptMessage(message)
            .getOrElse {
                ""
            }
    }

     fun sendMessage(text: String) {

         if(text.isBlank() || text.length>=5000) {
             sendUiEvent(ChatUiEvent.ShowToast("Text is blank or too long"))
             return
         }

         viewModelScope.launch {
             try {
                 _canSend.value = false
                 messageRepository.sendMessage(text, chatId)
                     .onFailure { error ->
                         sendUiEvent(ChatUiEvent.ShowToast("Error: ${error.message}"))
                     }
             } finally {
                 _canSend.value = true
             }
         }
    }

    fun deleteMessages(messages: List<Message>) {
        viewModelScope.launch {
            messageRepository.deleteMessages(messages)
                .onSuccess {
                    sendUiEvent(ChatUiEvent.ShowToast("Messages deleted"))
                }
                .onFailure { error ->
                    sendUiEvent(ChatUiEvent.ShowToast("Error: ${error.message}"))
                }
        }
    }

    fun editMessage(id: String, newText: String) {
        viewModelScope.launch {
            if(newText.isNotBlank() && newText.length<5000) {
                messageRepository.updateMessage(
                    item = MessageEntity(id, newText),
                    chatId = chatId
                ).onSuccess {
                    sendUiEvent(ChatUiEvent.ShowToast("Message edited"))
                }.onFailure { error ->
                    sendUiEvent(ChatUiEvent.ShowToast("Error: ${error.message}"))
                }
            } else {
                sendUiEvent(ChatUiEvent.ShowToast("Text is blank or too long"))
            }
        }
    }

    fun userTyping(value: String?) {
        viewModelScope.launch {
            userRepository.updateUserTyping(value)
                .onFailure { error ->
                    Log.e("Firestore","Error updating typing status", error)
                }
        }
    }

    private fun sendUiEvent(event: ChatUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}