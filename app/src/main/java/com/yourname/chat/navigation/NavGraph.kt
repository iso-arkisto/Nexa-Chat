package com.yourname.chat.navigation

import BanScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.yourname.chat.R
import com.yourname.chat.data.remote.UserPresenceManager
import com.yourname.chat.presentation.components.AppMenu
import com.yourname.chat.presentation.screen.account_screen.AccountScreen
import com.yourname.chat.presentation.screen.auth_screen.AuthScreen
import com.yourname.chat.presentation.screen.chat_screen.ChatScreen
import com.yourname.chat.presentation.screen.chatlist_screen.ChatListScreen
import com.yourname.chat.presentation.screen.userprofile_screen.UserProfileScreen
import com.yourname.chat.presentation.viewmodel.UsersViewModel
import com.yourname.chat.ui.theme.ChatTheme
import com.yourname.chat.utils.Screen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String?,
    usersViewModel: UsersViewModel = hiltViewModel()
) {
    ChatTheme {
        if(startDestination == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                composable("${Screen.Chat}/{chatId}") {
                    ChatScreen(onProfileClick = { userId ->
                        navController.navigate("${Screen.UserProfile}/$userId")
                    }, onReturn = {
                        navController.navigate(Screen.ChatList)
                    })
                }
                composable(Screen.Auth) {
                    AuthScreen(onSuccess = { destination ->
                        navController.navigate(destination) {
                            popUpTo(0) { inclusive = true }
                        }
                    })
                }
                composable(Screen.ChatList) {
                    AppMenu(
                        selectedTab = Screen.ChatList,
                        navController, mainContent = { paddingValues ->
                            ChatListScreen(onChatClick = { chatId ->
                                navController.navigate("${Screen.Chat}/$chatId")
                            }, paddingValues = paddingValues)
                        }
                    )
                }
                composable("${Screen.Ban}/{userId}") {
                    BanScreen(
                        onLogOut = {
                            usersViewModel.logOut()
                            navController.navigate(Screen.Auth)
                        },
                        onReturn = {
                            navController.navigate(Screen.ChatList)

                        }
                    )
                }
                composable("${Screen.UserProfile}/{userId}") {
                    UserProfileScreen(
                        onPopBackStack = { navController.popBackStack() },
                        onChatClick = { chatId ->
                            navController.navigate("${Screen.Chat}/$chatId")
                        }
                    )
                }
                composable(Screen.Account) {
                    AppMenu(
                        selectedTab = Screen.Account,
                        navController, mainContent = { paddingValues ->
                            AccountScreen(paddingValues = paddingValues)
                        }
                    )
                }

            }
        }
    }
}