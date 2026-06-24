package com.yourname.chat.presentation.screen.userprofile_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.yourname.chat.presentation.viewmodel.UsersViewModel
import com.yourname.chat.presentation.screen.chat_screen.components.AvatarCircle
import com.yourname.chat.ui.theme.PrimaryColor
import com.yourname.chat.R

@Composable
fun UserProfileScreen(
    uvm: UsersViewModel = hiltViewModel(),
    onPopBackStack: () -> Unit,
    onChatClick: (String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val userProfileState by viewModel.userState.collectAsStateWithLifecycle()
    val currentUserState by viewModel.currentUser.collectAsStateWithLifecycle()
    val userStatusState by viewModel.userStatus.collectAsStateWithLifecycle()

    val userProfile = userProfileState
    val currentUser = currentUserState
    val userStatus = userStatusState

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

    if(userProfile!=null && currentUser!=null) {

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
                           letter = userProfile.core.displayName.take(1),
                           color = PrimaryColor,
                           size = 125
                       )

                       if(userStatus?.state == "online") {
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
                       text = userProfile.core.displayName,
                       fontSize = 24.sp,
                       fontWeight = FontWeight.Bold,
                       color = MaterialTheme.colorScheme.onSurface
                   )
                   if(userProfile.core.verified) {
                       Spacer(modifier = Modifier.width(6.dp))
                       Icon(
                           painter = painterResource(R.drawable.check_circle),
                           contentDescription = "Verified",
                           tint = Color.Blue,
                           modifier = Modifier.size(20.dp)
                       )
                   }
               }

               if(userProfile.core.username!=null) {
                   Text(
                       text = "@${userProfile.core.username}",
                       fontSize = 16.sp,
                       color = Color.Gray
                   )
               }

               if(
                   userProfile.core.customStatus != null ||
                   userStatus?.geo != null ||
                   uvm.checkAccess(userProfile, userProfile.privacy.privacyEmail, currentUser) ||
                   (uvm.checkAccess(userProfile, userProfile.privacy.privacyPhoneNumber, currentUser) && userProfile.core.phoneNumber!=null) ||
                   userProfile.core.bio.isNotBlank()
               ) {
                   Spacer(modifier = Modifier.height(24.dp))

                   Surface(
                       modifier = Modifier.fillMaxWidth(),
                       shape = RoundedCornerShape(24.dp),
                       shadowElevation = 2.dp
                   ) {
                       Column(modifier = Modifier.padding(20.dp)) {
                           if(userProfile.core.customStatus!=null) {
                               ProfileInfoRow(
                                   userProfile.core.customStatus,
                                   R.drawable.check_circle
                               )
                           }


                           if(userStatus?.geo!=null) {
                               ProfileInfoRow(
                                   "${userStatus.geo.latitude}, ${userStatus.geo.longitude}",
                                   R.drawable.location_geo
                               )
                           }


                           if(uvm.checkAccess(userProfile, userProfile.privacy.privacyEmail, currentUser)) {
                               ProfileInfoRow(
                                   userProfile.core.email,
                                   R.drawable.mail
                               )
                           }

                           if(uvm.checkAccess(userProfile, userProfile.privacy.privacyPhoneNumber, currentUser) && userProfile.core.phoneNumber!=null) {
                               ProfileInfoRow(
                                   "+${userProfile.core.phoneNumber}",
                                   R.drawable.local_phone
                               )
                           }

                           Text(
                               text = userProfile.core.bio,
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
                           onChatClick(userProfile.core.uid)
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

                   if(userProfile.core.uid != currentUser.core.uid) {
                       FilledTonalButton(
                           onClick = { viewModel.addFriend() },
                           modifier = Modifier
                               .weight(1f)
                               .height(56.dp),
                           shape = RoundedCornerShape(16.dp),
                           colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE9ECEF))
                       ) {
                           Icon(
                               imageVector = when {
                                   userProfile.social.friends.contains(currentUser.core.uid) || currentUser.social.friends.contains(userProfile.core.uid) -> Icons.Default.Close
                                   userProfile.social.pendingFriendshipRequests.contains(currentUser.core.uid) -> Icons.Default.Close
                                   currentUser.social.pendingFriendshipRequests.contains(userProfile.core.uid) -> Icons.Default.Done
                                   else -> Icons.Default.Add
                               },
                               contentDescription = null,
                               tint = Color.Black
                           )
                           Spacer(modifier = Modifier.width(8.dp))
                           Text(
                               text = stringResource(
                                   when {
                                       userProfile.social.friends.contains(currentUser.core.uid) || currentUser.social.friends.contains(userProfile.core.uid) -> R.string.remove_friend
                                       userProfile.social.pendingFriendshipRequests.contains(currentUser.core.uid) -> R.string.cancel_friend_request
                                       currentUser.social.pendingFriendshipRequests.contains(userProfile.core.uid) -> R.string.accept_friend_request
                                       else -> R.string.add_to_friends
                                   }
                               ),
                               color = Color.Black
                           )
                       }
                   }
               }
           }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
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