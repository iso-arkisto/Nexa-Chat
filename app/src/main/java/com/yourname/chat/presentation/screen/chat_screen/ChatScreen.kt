package com.yourname.chat.presentation.screen.chat_screen

import ConfirmationDialog
import android.content.ClipData
import android.content.Context
import com.yourname.chat.R
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.chat.data.model.message.Message
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourname.chat.presentation.components.ErrorScreen
import com.yourname.chat.presentation.viewmodel.UsersViewModel
import com.yourname.chat.presentation.screen.chat_screen.components.ChatInputBar
import com.yourname.chat.presentation.screen.chat_screen.components.ChatMessageItem
import com.yourname.chat.ui.theme.PrimaryColor
import com.yourname.chat.utils.toShortTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    uvm: UsersViewModel = hiltViewModel(),
    onReturn: () -> Unit,
    onProfileClick: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val context: Context = LocalContext.current
    val clipboard = LocalClipboard.current

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState.value

    var selectedMessages by remember { mutableStateOf<List<Message>>(emptyList()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val text_copied = stringResource(R.string.text_copied)
    val wait_seconds = stringResource(R.string.wait_seconds)

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is ChatUiEvent.ShowToast -> {
                    Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    when(state) {
        is ChatUiState.Error -> {
            ErrorScreen(
                message = state.message.asString(),
                onRetry = { viewModel.retry() }
            )
        }

        is ChatUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is ChatUiState.Success -> {

            LaunchedEffect(state.allMessages.size) {
                if(state.allMessages.isNotEmpty()) {
                    listState.animateScrollToItem(listState.layoutInfo.totalItemsCount)
                }
            }

            if (showDeleteDialog) {
                ConfirmationDialog(
                    title = stringResource(R.string.delete_message),
                    desc = stringResource(R.string.action_undone),
                    agreeLabel = stringResource(R.string.delete),
                    onCancel = {
                        showDeleteDialog = false
                    },
                    onAgree = {
                        showDeleteDialog = false
                        viewModel.deleteMessages(messages = selectedMessages)
                        selectedMessages = emptyList()
                    },
                    isDangerous = true
                )
            }

            if(showEditDialog) {
                var decryptedText by remember { mutableStateOf<String?>(state.allMessages.find { it.id == selectedMessages.firstOrNull()?.id }?.text ?: "") }

                if(decryptedText!=null) {
                    ConfirmationDialog(
                        title = stringResource(R.string.edit_message),
                        agreeLabel = stringResource(R.string.edit),
                        textFieldValue = decryptedText ?: "",
                        onCancel = { showEditDialog = false },
                        onAgreeWithText = { text ->
                            showEditDialog = false

                            if(selectedMessages.size==1) {
                                viewModel.editMessage(selectedMessages.firstOrNull()?.id ?: "", text)
                                selectedMessages = emptyList()
                            }
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.userTyping(null)
                            onReturn()
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        onProfileClick(state.targetUser.core.uid)
                                    }
                                    .background(PrimaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.userAvatar,
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = state.chatTitle.asString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = if(state.targetUser.core.uid == state.currentUser.core.uid) {
                                        stringResource(R.string.self_messages)
                                    } else {
                                        if(state.userStatus.typing == viewModel.chatId) {
                                            "${stringResource(R.string.typing).lowercase()}..."
                                        } else if(state.userStatus.state == "online") {
                                            stringResource(R.string.online)
                                        } else if(state.userStatus.lastSeen != null) {
                                            context.getString(R.string.last_seen, state.userStatus.lastSeen.toShortTimeString())
                                        } else {
                                            stringResource(R.string.offline)
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        if(selectedMessages.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if(selectedMessages.size == 1) {
                                    Button(
                                        onClick = {

                                            scope.launch {
                                                val decryptedText = state.allMessages.find { it.id == selectedMessages.firstOrNull()?.id }?.text ?: ""
                                                clipboard.setClipEntry(
                                                    ClipEntry(ClipData.newPlainText("label",decryptedText))
                                                )
                                                selectedMessages = emptyList()
                                                Toast.makeText(context, text_copied, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .clip(CircleShape),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.copy),
                                            contentDescription = "Copy",
                                            modifier = Modifier
                                                .size(25.dp),
                                            colorFilter = ColorFilter.tint(PrimaryColor)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width((-100).dp))
                                    if(!state.currentUser.moderation.chatAccess.banned && selectedMessages[0].senderId == state.currentUser.core.uid) {
                                        Button(
                                            onClick = {
                                                showEditDialog = true
                                            },
                                            modifier = Modifier
                                                .clip(CircleShape),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent
                                            )
                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.edit),
                                                contentDescription = stringResource(R.string.edit),
                                                modifier = Modifier
                                                    .size(25.dp),
                                                colorFilter = ColorFilter.tint(PrimaryColor)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width((-10).dp))

                                    }



                                }
                                if(selectedMessages.all { it.senderId == state.currentUser.core.uid || (System.currentTimeMillis() - (it.timestamp?.toDate()?.time ?: 61_000)) < 600_000 }) {
                                    Button(
                                        onClick = {
                                            showDeleteDialog = true
                                        },
                                        modifier = Modifier
                                            .clip(CircleShape),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.delete),
                                            contentDescription = stringResource(R.string.delete),
                                            modifier = Modifier
                                                .size(25.dp),
                                            colorFilter = ColorFilter.tint(PrimaryColor)
                                        )
                                    }
                                }

                            }
                        }
                    }
                )


            }


            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.9f)
                    .padding(top = 80.dp)
            ) {
                items(state.allMessages) {
                        msg ->

                    val isSentByCurrentUser = state.currentUser.core.uid == msg.senderId

                    ChatMessageItem(
                        item = msg,
                        isMine = isSentByCurrentUser,
                        onContextMenu = {
                            if(selectedMessages.isEmpty()) {
                                selectedMessages += msg
                            }
                        },
                        onClick = {if(selectedMessages.isNotEmpty()) {
                            if(selectedMessages.contains(msg)) {
                                selectedMessages -= msg
                            } else {
                                selectedMessages += msg
                            }
                        } },
                        isSelected = selectedMessages.contains(msg),
                    )
                }
            }

            ChatInputBar(onMessageSend = { text ->
                if (state.canSend) {
                    if(!state.currentUser.moderation.chatAccess.banned && uvm.checkAccess(state.currentUser, state.targetUser.privacy.whoCanChat, state.targetUser)) {
                        viewModel.sendMessage(text)
                    }
                } else {
                    Toast.makeText(context, wait_seconds, Toast.LENGTH_SHORT).show()
                }
            }, onType = {
                viewModel.userTyping(viewModel.chatId)
            },
                banReason =
                    if(state.currentUser.moderation.chatAccess.banned) {
                        stringResource(R.string.your_chat_restricted)
                    } else {
                        if(!uvm.checkAccess(state.currentUser, state.targetUser.privacy.whoCanChat, state.targetUser) && state.currentUser.core.uid != state.targetUser.core.uid) {
                            stringResource(R.string.who_can_message)
                        } else {
                            null
                        }
                    }
            )
        }
    }
}
