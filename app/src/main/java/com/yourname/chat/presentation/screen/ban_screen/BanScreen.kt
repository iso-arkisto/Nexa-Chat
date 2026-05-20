import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.database.ServerValue
import com.yourname.chat.BuildConfig
import com.yourname.chat.ui.theme.BackgroundGradient
import com.yourname.chat.R
import com.yourname.chat.presentation.screen.ban_screen.BanUiEvent
import com.yourname.chat.presentation.screen.ban_screen.BanUiState
import com.yourname.chat.presentation.screen.ban_screen.BanViewModel
import com.yourname.chat.utils.toFullDateString
import com.yourname.chat.utils.toShortTimeString
import kotlinx.coroutines.delay
import com.yourname.chat.data.manager.ServerTimeManager

@Composable
fun BanScreen(
    viewModel: BanViewModel = hiltViewModel(),
    onLogOut: () -> Unit,
    onReturn: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is BanUiEvent.ShowToast -> {
                    Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(viewModel.userId == "not_working" || viewModel.userId.contains("old_version")) {
                    Text(
                        text = stringResource(R.string.access_restricted),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 20.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )

                    Text(
                        text = "${stringResource(R.string.reason)}:",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Text(
                        text = if(viewModel.userId == "not_working") {
                            stringResource(R.string.messenger_down)
                        } else {
                            context.getString(R.string.outdated_version, BuildConfig.VERSION_NAME, viewModel.userId.split("_")[2])
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    when (uiState) {
                        is BanUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }

                        is BanUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Error: ${(uiState as BanUiState.Error).message}! Check your internet connection")
                            }
                        }

                        is BanUiState.Success -> {
                            val data = (uiState as BanUiState.Success).data

                            if (data != null) {
                                if (!data.banned) {
                                    onReturn()
                                }

                                viewModel.changeUserStatus(mapOf(
                                    "state" to "offline",
                                    "lastSeen" to ServerValue.TIMESTAMP,
                                    "typing" to null,
                                    "geo" to null
                                ))

                                val serverTimeBase = ServerTimeManager.getNow()
                                var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

                                LaunchedEffect(Unit) {
                                    while (true) {
                                        currentTime = System.currentTimeMillis()
                                        delay(1000)
                                    }
                                }

                                val exactTime = currentTime + (serverTimeBase - System.currentTimeMillis())

                                LaunchedEffect(exactTime) {
                                    if(data.endTime!=null) {
                                        if(data.endTime - exactTime < 0) {
                                            viewModel.unbanUser()
                                        }
                                    }
                                }

                                Text(
                                    text = stringResource(R.string.access_restricted),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Image(
                                    painter = painterResource(R.drawable.banned3),
                                    contentDescription = "Banned",
                                    modifier = Modifier.size(300.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                BanInfoRow(
                                    label = "${stringResource(R.string.start)}:",
                                    value = data.startTime.toFullDateString()
                                )
                                if (data.endTime != null) {
                                    BanInfoRow(
                                        label = "${stringResource(R.string.end)}:",
                                        value = data.endTime.toFullDateString()
                                    )
                                }
                                BanInfoRow(
                                    label = "${stringResource(R.string.duration)}:",
                                    value = if (data.endTime == null) stringResource(R.string.permanently) else data.endTime.toShortTimeString()
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 20.dp),
                                    color = Color.White.copy(alpha = 0.1f)
                                )

                                Text(
                                    text = "${stringResource(R.string.reason)}:",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = data.reason ?: stringResource(R.string.not_specified),
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                Button(
                                    onClick = {
                                        onLogOut()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.1f),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.log_out),
                                        fontWeight = FontWeight.SemiBold
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



@Composable
fun BanInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
