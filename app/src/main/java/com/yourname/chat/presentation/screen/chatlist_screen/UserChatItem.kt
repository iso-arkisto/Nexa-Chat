package com.yourname.chat.presentation.screen.chatlist_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.chat.ui.theme.PrimaryColor

@Composable
fun UserChatItem(
    onClick: () -> Unit,
    state: UserChatUiState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = if(state.isOnline && !state.isStorage) {
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(state.avatarColor)
                    .border(4.dp, Color.Green, CircleShape)
            } else {
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(state.avatarColor)
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.avatarLabel,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.displayName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val messageColor = if (state.lastMessageText == null) Color.Gray else Color.DarkGray

            Text(
                text = state.lastMessageText ?: "Empty",
                style = MaterialTheme.typography.bodyMedium,
                color = messageColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}