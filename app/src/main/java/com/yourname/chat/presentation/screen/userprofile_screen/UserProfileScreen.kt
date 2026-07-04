package com.yourname.chat.presentation.screen.userprofile_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourname.chat.presentation.screen.chat_screen.components.AvatarCircle
import com.yourname.chat.ui.theme.PrimaryColor
import com.yourname.chat.R
import com.yourname.chat.presentation.components.ErrorScreen

@Composable
fun UserProfileScreen(
    onPopBackStack: () -> Unit,
    onChatClick: (String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState.value

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is UserProfileUiEvent.ShowToast -> {
                    Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    when(state) {
        is UserProfileUiState.Error -> {
            ErrorScreen(
                message = state.message.asString(),
                onRetry = { viewModel.retry() }
            )
        }
        is UserProfileUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is UserProfileUiState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(40.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = {
                        onPopBackStack()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(70.dp))
                    Box(contentAlignment = Alignment.BottomEnd) {

                        AvatarCircle(
                            letter = state.targetUser.core.displayName.take(1),
                            color = PrimaryColor,
                            size = 125
                        )

                        if(state.userStatus?.state == "online") {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = state.targetUser.core.displayName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if(state.targetUser.core.verified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(R.drawable.check_circle),
                            contentDescription = "Verified",
                            tint = Color.Blue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if(state.targetUser.core.username!=null) {
                    Text(
                        text = "@${state.targetUser.core.username}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }

                if(state.isProfileInfoVisible) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            if(state.targetUser.core.customStatus!=null) {
                                ProfileInfoRow(
                                    state.targetUser.core.customStatus,
                                    R.drawable.check_circle
                                )
                            }


                            if(state.userStatus?.geo!=null) {
                                ProfileInfoRow(
                                    "${state.userStatus.geo.latitude}, ${state.userStatus.geo.longitude}",
                                    R.drawable.location_geo
                                )
                            }


                            if(state.canSeeEmail) {
                                ProfileInfoRow(
                                    state.targetUser.core.email,
                                    R.drawable.mail
                                )
                            }

                            if(state.canSeePhone) {
                                ProfileInfoRow(
                                    "+${state.targetUser.core.phoneNumber}",
                                    R.drawable.local_phone
                                )
                            }

                            Text(
                                text = state.targetUser.core.bio,
                                fontSize = 17.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            onChatClick(state.targetUser.core.uid)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.mail),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.message),
                            color = MaterialTheme.colorScheme.surface
                        )
                    }

                    if(state.friendButtonState.isVisible) {
                        FilledTonalButton(
                            onClick = { viewModel.addFriend() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE9ECEF))
                        ) {
                            Icon(
                                imageVector = state.friendButtonState.icon,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(state.friendButtonState.textResId),
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    text: String,
    icon: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 12.dp),
        thickness = 0.5.dp,
        color = Color(0xFF8F8F8F)
    )
}