package com.yourname.chat.presentation.screen.chat_screen.components

import com.yourname.chat.R
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.chat.data.model.message.Message
import com.yourname.chat.ui.theme.PrimaryColor
import com.yourname.chat.utils.toShortTimeString

@Composable
fun ChatMessageItem(
    item: Message,
    isMine: Boolean,
    onContextMenu: () -> Unit,
    onClick: () -> Unit,
    isSelected: Boolean,
) {

    val timeString = remember(item.timestamp) {
        item.timestamp?.toDate()?.time?.toShortTimeString() ?: "???"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {

        Card(
            shape = RoundedCornerShape(
                topStart = 32.dp,
                topEnd = 32.dp,
                bottomStart = if (isMine) 32.dp else 8.dp,
                bottomEnd = if (isMine) 8.dp else 32.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isSelected -> PrimaryColor
                    isMine -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .pointerInput(item.id) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { onContextMenu() }
                    )
                }
        ) {
            Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp)) {

                Text(
                    text = item.text ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )

                Text(
                    text = "$timeString ${if(item.edited != null) "\n${stringResource(R.string.edited)}" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .align(if(isMine) Alignment.End else Alignment.Start)
                        .padding(top = 4.dp)
                )
            }
        }

    }
}

