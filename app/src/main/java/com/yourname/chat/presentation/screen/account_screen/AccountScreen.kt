package com.yourname.chat.presentation.screen.account_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourname.chat.R
import com.yourname.chat.presentation.screen.auth_screen.SettingItem
import com.yourname.chat.ui.theme.PrimaryColor
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    viewModel: AccountViewModel = hiltViewModel(),
    paddingValues: PaddingValues
) {

    var writingDisplayName by remember { mutableStateOf("") }
    var writingBio by remember { mutableStateOf("") }
    var writingPhoneNumber by remember { mutableStateOf("") }
    var writingUsername by remember { mutableStateOf("") }

    val username_taken = stringResource(R.string.username_taken)
    val empty_name = stringResource(R.string.empty_name)

    val scope = rememberCoroutineScope()

    val sections = listOf(
        SettingItem(stringResource(R.string.profile), R.drawable.person),
        SettingItem(stringResource(R.string.security), R.drawable.lock),
        SettingItem(stringResource(R.string.privacy), R.drawable.chat),
        SettingItem(stringResource(R.string.social), R.drawable.group),
        SettingItem(stringResource(R.string.wallet), R.drawable.payments),
        SettingItem(stringResource(R.string.general), R.drawable.more_horiz)
    )

    var selectedTabIndex by remember { mutableIntStateOf(-1) }
    val context = LocalContext.current

    val profileTextFields = listOf(
        R.string.your_name,
        R.string.username,
        R.string.bio,
       R.string.phone_number
    )

    val userState by viewModel.userState.collectAsStateWithLifecycle()
    val user = userState

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is AccountUiEvent.ShowToast -> {
                    Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {


            if (user != null) {

                LaunchedEffect(user) {
                    user.let {
                        writingDisplayName = user.core.displayName
                        writingBio = user.core.bio
                        writingUsername = user.core.username ?: ""
                        writingPhoneNumber = user.core.phoneNumber ?: ""
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))


                    if(selectedTabIndex < 0) {
                        Text(
                            text = stringResource(R.string.account_settings),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        if (user.core.photoUrl == null) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryColor),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = user.core.displayName.take(1).uppercase(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(user.core.displayName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        if (user.core.username != null) {
                            Text(
                                "@${user.core.username}",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))


                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(3.dp),
                            verticalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            sections.forEach {
                                SettingRow(it, onClick = {
                                    selectedTabIndex = sections.indexOf(it)
                                })
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            IconButton(
                                onClick = { selectedTabIndex = -1 }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back),
                                    contentDescription = "Back"
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = sections[selectedTabIndex].title,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp, end = 50.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))

                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        when(selectedTabIndex) {
                            0 -> {
                                if (user.core.photoUrl == null) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryColor),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = user.core.displayName.take(1).uppercase(),
                                            color = Color.White,
                                            style = MaterialTheme.typography.headlineLarge
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(15.dp))

                                profileTextFields.forEach {
                                        label ->
                                    OutlinedTextField(
                                        value = when(label) {
                                            R.string.your_name -> writingDisplayName
                                            R.string.username -> writingUsername
                                            R.string.bio -> writingBio
                                            R.string.phone_number -> writingPhoneNumber
                                            else -> "???"
                                        },
                                        onValueChange = {
                                            when(label) {
                                                R.string.your_name -> if(it.length <= 25) writingDisplayName = it
                                                R.string.username -> if (it.length <= 20 && (Regex("^[a-zA-Z0-9_-]+\$").matches(it) || it.isBlank())) writingUsername = it
                                                R.string.bio -> if (it.length <= 500) writingBio = it
                                                R.string.phone_number -> {
                                                    if (it.length <= 15 && it.all { letter -> letter.isDigit() }) {
                                                        writingPhoneNumber = it
                                                    }
                                                }
                                            }
                                        },
                                        label = { Text(stringResource(label)) },
                                        supportingText = { Text(
                                            text = when(label) {
                                                R.string.your_name -> "${writingDisplayName.length}/25"
                                                R.string.username -> "${writingUsername.length}/20"
                                                R.string.bio -> "${writingBio.length}/500"
                                                R.string.phone_number -> "${writingPhoneNumber.length}/15"
                                                else -> "???"
                                            }
                                        ) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = label != R.string.bio,
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = if(label == R.string.phone_number) KeyboardType.Phone else KeyboardType.Text
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                Spacer(modifier = Modifier.height(40.dp))

                                Button(
                                    onClick = {

                                        if(!isLoading) {
                                            scope.launch {
                                                when {
                                                    writingDisplayName.isBlank() -> {
                                                        Toast.makeText(context, empty_name, Toast.LENGTH_SHORT).show()
                                                    }
                                                    !viewModel.checkUsernameAvailability(writingUsername) -> {
                                                        Toast.makeText(context, username_taken, Toast.LENGTH_SHORT).show()
                                                    }
                                                    else -> {
                                                        viewModel.updateAccountData(hashMapOf(
                                                            ("core.displayName" to writingDisplayName),
                                                            ("core.bio" to writingBio),
                                                            ("core.username" to writingUsername.ifBlank { null }),
                                                            ("core.phoneNumber" to writingPhoneNumber.ifBlank { null })
                                                        ))
                                                    }
                                                }
                                            }

                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    enabled = !isLoading
                                ) {
                                    Text(if (isLoading) stringResource(R.string.loading) else stringResource(R.string.save_changes))
                                }
                            }
                        }
                    }


                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
}