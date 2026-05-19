package com.yourname.chat.presentation.screen.auth_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourname.chat.R
import com.yourname.chat.ui.theme.BackgroundGradient
import com.yourname.chat.ui.theme.PrimaryColor

data class SettingItem(
    val title: String,
    val icon: Int,
)

@Composable
fun AuthScreen(
    onSuccess: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var writingEmail by remember { mutableStateOf("") }
    var writingPass by remember { mutableStateOf("") }

    var authError by remember { mutableStateOf("") }
    var loggingIn by remember { mutableStateOf(true) }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is AuthUiEvent.Success -> {
                    onSuccess(event.route)
                }
                is AuthUiEvent.Error -> {
                    authError = event.message
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(loggingIn) {
                    Text(
                        text = stringResource(R.string.welcome_back),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = stringResource(R.string.please_log_in),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )
                } else {
                    Text(
                        text = "${stringResource(R.string.welcome_to)} ${stringResource(R.string.app_name)}!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }

                AuthTextField(
                    value = writingEmail,
                    placeholder = "Email",
                    onValueChange = { newText ->
                        authError = ""
                        if(newText.length <= 100) {
                            writingEmail = newText
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = writingPass,
                    placeholder = stringResource(R.string.password),
                    onValueChange = { newText ->
                        authError = ""
                        if(newText.length <= 50) {
                            writingPass = newText
                        }
                    },
                    hideLetters = true
                )

                Spacer(modifier = Modifier.height(10.dp))
                if(authError.isNotBlank()) {
                    Text(
                        authError,
                        color = Color.Red,
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if(!isLoading) {
                            if(loggingIn) {
                                viewModel.signIn(
                                    writingEmail,
                                    writingPass
                                )
                            } else {
                                viewModel.signUp(
                                    writingEmail,
                                    writingPass
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text(
                        text = if(isLoading) "${stringResource(R.string.loading)}..." else if(loggingIn) stringResource(R.string.log_in) else stringResource(R.string.sign_up),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                if(loggingIn) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${stringResource(R.string.no_account)} ",
                        )
                        Text(
                            text = stringResource(R.string.sign_up),
                            color = PrimaryColor,
                            modifier = Modifier.clickable {
                                loggingIn = false
                                authError = ""

                            }
                        )
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${stringResource(R.string.have_account)} ",
                        )
                        Text(
                            text = stringResource(R.string.log_in),
                            color = PrimaryColor,
                            modifier = Modifier.clickable {
                                loggingIn = true
                                authError = ""
                            }
                        )
                    }
                }
            }
        }
    }
}