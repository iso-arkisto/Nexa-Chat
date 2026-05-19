package com.yourname.chat.presentation.screen.chatlist_screen

import ConfirmationDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourname.chat.data.model.chat.Chat
import com.yourname.chat.ui.theme.PrimaryColor
import com.yourname.chat.R
import com.yourname.chat.utils.ServerTimeManager
import com.yourname.chat.utils.toShortTimeString
import kotlinx.coroutines.delay

@Composable
fun ChatListScreen(
    onChatClick: (String) -> Unit,
    viewModel: ChatListViewModel = hiltViewModel(),
    paddingValues: PaddingValues
) {
    var searchField by remember { mutableStateOf("") }

    var searchSelectedIndex by remember { mutableIntStateOf(0) }
    val searchItems = listOf(stringResource(R.string.users), stringResource(R.string.groups))

    var chatAccessDialog by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val connectedUsers by viewModel.connectedUsers.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    val serverTimeBase = ServerTimeManager.getNow()
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(chatAccessDialog) {
        if(chatAccessDialog) {
            while (true) {
                currentTime = System.currentTimeMillis()
                delay(1000)
                if(!chatAccessDialog) break
            }
        }
    }

    val exactTime = currentTime + (serverTimeBase - System.currentTimeMillis())
    val context = LocalContext.current

    if(chatAccessDialog && currentUser != null) {
        val reason = currentUser?.moderation?.chatAccess?.reason ?: stringResource(R.string.not_specified)
        val timeLeft =if(currentUser?.moderation?.chatAccess?.endTime == null) stringResource(R.string.permanently) else (currentUser!!.moderation.chatAccess.endTime!!- exactTime).toShortTimeString()
        ConfirmationDialog(
            title = stringResource(R.string.sending_restricted),
            desc = context.getString(R.string.sending_access, reason, timeLeft),
            onlyOK = true,
            onAgree = { chatAccessDialog = false },
            onCancel = { },
            agreeLabel = "ОК"
        )
    }


            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(start = 25.dp, top = 25.dp)
                    .fillMaxSize()
            ) {

                Text(
                    text = stringResource(R.string.chats),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )


                OutlinedTextField(
                    value = searchField,
                    onValueChange = {
                        searchField = it
                    },
                    placeholder = { Text("${stringResource(R.string.search)}...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    trailingIcon = {
                        if (searchField.isNotBlank()) {
                            IconButton(onClick = { searchField = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
                if(searchField.isBlank()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(connectedUsers) {
                            item ->

                            UserChatItem(
                                onClick = { onChatClick(item.id) },
                                state = item
                            )
                        }
                    }
                } else {
                    val color = MaterialTheme.colorScheme.onSurface

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        searchItems.forEachIndexed { index, title ->
                            val isSelected = searchSelectedIndex == index

                            val underlineProgress by animateFloatAsState(
                                targetValue = if (isSelected) 1f else 0f,
                                animationSpec = tween(durationMillis = 200),
                                label = "UnderlineAnimation"
                            )

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { searchSelectedIndex = index }
                                    .padding(vertical = 4.dp)
                                    .drawBehind {
                                        if (underlineProgress > 0f) {
                                            val strokeWidth = 2.dp.toPx()
                                            val y = size.height

                                            val lineWidth = size.width * underlineProgress
                                            val startX = (size.width - lineWidth) / 2

                                            drawLine(
                                                color = color,
                                                start = Offset(startX, y),
                                                end = Offset(startX + lineWidth, y),
                                                strokeWidth = strokeWidth
                                            )
                                        }
                                    }
                            )
                        }
                    }


                    val foundUsers by remember(allUsers, searchField) { mutableStateOf(
                        allUsers.filter { user ->
                        (user.core.displayName.contains(searchField, ignoreCase = true) || user.core.username?.contains(searchField, ignoreCase = true) ?: false) &&
                                user.privacy.privacySearch &&
                                !user.social.blockedUsers.contains(currentUser?.core?.uid ?: "") &&
                                !(currentUser?.social?.blockedUsers?.contains(user.core.uid) ?: false)
                    }) }

                    val foundChats by remember { mutableStateOf(emptyList<Chat>()) }

                    if((foundChats.isEmpty() && searchSelectedIndex == 1) || (foundUsers.isEmpty() && searchSelectedIndex == 0)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            Text(
                                text = stringResource(R.string.no_results),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.change_query),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(0.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            if(searchSelectedIndex == 0) {
                                items(foundUsers) { chat ->

                                    val hexColor = PrimaryColor

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onChatClick(chat.core.uid)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(hexColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = chat.core.displayName.take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = chat.core.displayName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if(chat.core.username?.isNotBlank() ?: false) {
                                                Text(
                                                    text = "@${chat.core.username}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(foundChats) { chat ->

                                    val decryptedName = chat.name
                                    val decryptedLogin: String = chat.link ?: ""
                                    val members = ""

                                    val hexColor = PrimaryColor

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(hexColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = decryptedName.take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = decryptedName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = if(decryptedLogin.isNotBlank()) "@${decryptedLogin}, $members members" else "$members members",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }

}