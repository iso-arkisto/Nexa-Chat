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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.yourname.chat.presentation.components.UiText
import com.yourname.chat.presentation.screen.chat_screen.components.ChatInputBar
import com.yourname.chat.presentation.screen.chat_screen.components.ChatMessageItem
import com.yourname.chat.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onReturn: () -> Unit,
    onProfileClick: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val context: Context = LocalContext.current
    val clipboard = LocalClipboard.current

    val listState = rememberLazyListState()

    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState.value

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is ChatUiEvent.ShowToast -> {
                    val toastText = when (val uiText = event.text) {
                        is UiText.DynamicString -> uiText.value
                        is UiText.ResourceString -> context.getString(uiText.resId, listOf(uiText.args))
                    }

                    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                }
                is ChatUiEvent.CopyToClipboard -> {
                    clipboard.setClipEntry(
                        ClipEntry(ClipData.newPlainText("label", event.text))
                    )
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

            if (state.dialogs is ChatDialogsState.DeleteMessage) {
                ConfirmationDialog(
                    title = stringResource(R.string.delete_message),
                    desc = stringResource(R.string.action_undone),
                    agreeLabel = stringResource(R.string.delete),
                    onCancel = { viewModel.onDismissDialog() },
                    onAgree = {
                        viewModel.onConfirmDeleteMessage()
                    },
                    isDangerous = true
                )
            }

            if(state.dialogs is ChatDialogsState.EditMessage) {
                var decryptedText by remember { mutableStateOf<String?>(state.allMessages.find { it.id == state.selectedMessages.firstOrNull()?.id }?.text ?: "") }

                if(decryptedText!=null) {
                    ConfirmationDialog(
                        title = stringResource(R.string.edit_message),
                        agreeLabel = stringResource(R.string.edit),
                        textFieldValue = decryptedText ?: "",
                        onCancel = { viewModel.onDismissDialog() },
                        onAgreeWithText = { text ->
                            viewModel.onConfirmEditMessage(text)
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
                                    text = state.chatHeader.avatar,
                                    color = Color.White,
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = state.chatHeader.title.asString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = state.chatHeader.status.asString(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        if(state.selectedMessages.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if(state.selectedMessages.size == 1) {
                                    Button(
                                        onClick = { viewModel.copyMessageText() },
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

                                    if(state.chatHeader.canEditMessage) {
                                        Button(
                                            onClick = { viewModel.onEditMessageDialogOpen() },
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

                                if(state.chatHeader.canDeleteMessages) {
                                    Button(
                                        onClick = { viewModel.onDeleteMessageDialogOpen() },
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
                        onContextMenu = { viewModel.toggleMessageSelection(msg) },
                        onClick = {
                            if(state.selectedMessages.isNotEmpty()) {
                                viewModel.toggleMessageSelection(msg)
                            }
                        },
                        isSelected = state.selectedMessages.contains(msg),
                    )
                }
            }

            ChatInputBar(onMessageSend = { text ->
                if (state.messageInput.canSend) {
                    viewModel.sendMessage(text)
                } else {
                    val message = context.getString(R.string.wait_seconds)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }, onType = {
                viewModel.userTyping(viewModel.chatId)
            },
                banReason = state.messageInput.restrictionReason?.asString()
            )
        }
    }
}
