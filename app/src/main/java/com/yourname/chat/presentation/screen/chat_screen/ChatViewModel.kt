package com.yourname.chat.presentation.screen.chat_screen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.chat.R
import com.yourname.chat.data.local.MessageEntity
import com.yourname.chat.data.model.message.Message
import com.yourname.chat.data.model.user.User
import com.yourname.chat.data.model.user.UserStatus
import com.yourname.chat.domain.repository.MessageRepository
import com.yourname.chat.domain.repository.UserRepository
import com.yourname.chat.presentation.components.UiText
import com.yourname.chat.utils.toShortTimeString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    val chatId: String? = savedStateHandle["chatId"]

    private val _uiEvent = Channel<ChatUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    private val _canSend = MutableStateFlow(true)
    private val _currentDialog = MutableStateFlow<ChatDialogsState>(ChatDialogsState.None)

    val uiState: StateFlow<ChatUiState> = retryTrigger
        .flatMapLatest {

            if(chatId.isNullOrBlank()) {
                return@flatMapLatest flowOf(
                    ChatUiState.Error(UiText.DynamicString("Chat ID is missing"))
                )
            }

            val decryptedMessagesFlow = messageRepository.getMessages(chatId)
                .map { messages ->
                    messages.map { msg ->
                        msg.copy(text = decryptMessage(msg))
                    }
                }

            val domainDataFlow = combine(
                decryptedMessagesFlow,
                userRepository.getUserData(chatId),
                userRepository.getCurrentUserData(),
                userRepository.getUserStatus(chatId)
            ) { allMessages, target, current, status ->
                if(allMessages != null && target != null && current != null && status != null) {
                    DomainChatData(allMessages, target, current, status)
                } else {
                    null
                }
            }

            val localUiFlow = combine(
                _canSend,
                _currentDialog
            ) { canSend, currentDialog ->
                LocalUiData(canSend, currentDialog)
            }

            combine(
                domainDataFlow,
                localUiFlow
            ) { domainData, localUi ->

                if(domainData != null) {
                    val isOwnProfile = domainData.target.core.uid == domainData.current.core.uid

                    val chatTitle = if(isOwnProfile) UiText.ResourceString(R.string.storage) else UiText.DynamicString(domainData.target.core.displayName)
                    val userAvatar = if(isOwnProfile) "🔒" else domainData.target.core.displayName.take(1)

                    val statusText = when {
                        isOwnProfile -> { UiText.ResourceString(R.string.self_messages) }
                        domainData.status.typing == chatId -> { UiText.ResourceString(R.string.typing) }
                        domainData.status.state == "online" -> { UiText.ResourceString(R.string.online) }
                        domainData.status.lastSeen != null -> {
                            val formattedTime = domainData.status.lastSeen.toShortTimeString()
                            UiText.ResourceString(R.string.last_seen, listOf(formattedTime))
                        }
                        else -> { UiText.ResourceString(R.string.offline) }
                    }

                    ChatUiState.Success(
                        allMessages = domainData.allMessages,
                        chatHeader = ChatHeaderState(
                            title = chatTitle,
                            avatar = userAvatar,
                            status = statusText
                        ),
                        messageInput = MessageInputState(
                            canSend = localUi.canSend
                        ),
                        dialogs = localUi.currentDialog,
                        targetUser = domainData.target,
                        currentUser = domainData.current
                    )
                } else {
                    ChatUiState.Loading
                }
            }
                .catch { e ->
                    emit(ChatUiState.Error(UiText.DynamicString(e.localizedMessage ?: "Unknown error")))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatUiState.Loading
        )

    fun retry() {
        retryTrigger.tryEmit(Unit)
    }

    fun onDismissDialog() {
        uiState
    }

     fun sendMessage(text: String) {
         val currentState = uiState.value as? ChatUiState.Success ?: return

         if(text.isBlank() || text.length>=5000) {
             sendUiEvent(ChatUiEvent.ShowToast("Text is blank or too long"))
             return
         }

         viewModelScope.launch {
             try {
                 _canSend.value = false
                 messageRepository.sendMessage(text, chatId!!)
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

        val currentState = uiState.value as? ChatUiState.Success ?: return

        viewModelScope.launch {
            if(newText.isNotBlank() && newText.length<5000) {
                messageRepository.updateMessage(
                    item = MessageEntity(id, newText),
                    chatId = chatId!!
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

    private suspend fun decryptMessage(message: Message): String {
        return messageRepository.decryptMessage(message)
            .getOrElse {
                ""
            }
    }

    private data class DomainChatData(
        val allMessages: List<Message>,
        val target: User,
        val current: User,
        val status: UserStatus
    )

    private data class LocalUiData(
        val canSend: Boolean,
        val currentDialog: ChatDialogsState
    )

}