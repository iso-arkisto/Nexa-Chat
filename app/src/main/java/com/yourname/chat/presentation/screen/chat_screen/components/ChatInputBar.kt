package com.yourname.chat.presentation.screen.chat_screen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.yourname.chat.R

@Composable
fun ChatInputBar(
    onMessageSend: (String) -> Unit,
    banReason: String? = null,
    onType: () -> Unit
) {
    var messageText by rememberSaveable { mutableStateOf("") }
    val canSend = messageText.isNotBlank() && banReason == null
    var lastTypingTime by remember { mutableLongStateOf(0L) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1000f)
                .padding(horizontal = 16.dp, vertical = 0.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = messageText,
                    onValueChange = {
                        messageText = it
                        val now = System.currentTimeMillis()
                        if(now - lastTypingTime > 2000) {
                            lastTypingTime = now
                            onType()
                        }
                    },
                    placeholder = { Text(
                        text = banReason ?: "${stringResource(R.string.message)}...",
                        color = Color.Gray
                    ) },
                    modifier = Modifier
                        .weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    maxLines = 4,
                    enabled = banReason == null
                )



                    IconButton(
                        onClick = {
                            onMessageSend(messageText)
                            messageText = ""
                        },
                        enabled = canSend
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.send),
                            contentDescription = "Send",
                            tint = if (messageText.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
            }
        }
    }
}