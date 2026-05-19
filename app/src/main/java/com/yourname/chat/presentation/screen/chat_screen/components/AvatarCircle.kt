package com.yourname.chat.presentation.screen.chat_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourname.chat.ui.theme.PrimaryColor

@Composable
fun AvatarCircle(
    letter: String,
    color: Color = PrimaryColor,
    size: Int = 45,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clickable { onClick() }
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.uppercase(),
            color = Color.White,
            fontSize = (size*.37).sp,
            fontWeight = FontWeight.Bold
        )
    }
}