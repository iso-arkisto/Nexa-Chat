package com.yourname.chat.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.yourname.chat.R
import com.yourname.chat.presentation.viewmodel.UsersViewModel
import com.yourname.chat.utils.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppMenu(
    selectedTab: String,
    navController: NavController,
    uvm: UsersViewModel = hiltViewModel(),
    mainContent: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentUserState by uvm.currentUser.collectAsStateWithLifecycle()
    val currentUser = currentUserState

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.tabs), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
                    HorizontalDivider()

                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.account)) },
                        selected = selectedTab == Screen.Account,
                        icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                        onClick = {
                            navController.navigate(Screen.Account)
                            scope.launch { drawerState.close() }
                        },
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.chats)) },
                        selected = selectedTab == Screen.ChatList,
                        icon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                        onClick = {
                            navController.navigate(Screen.ChatList)
                            scope.launch { drawerState.close() }
                        },
                    )
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.app_name))  },
                        modifier = Modifier.background(MaterialTheme.colorScheme.background),
                        navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) {
                                            drawerState.open()
                                        } else {
                                            drawerState.close()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                        },
                        actions = {
                            if(selectedTab == Screen.Account) {
                                Row(
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.feather),
                                        contentDescription = "Feathers",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = currentUser?.metadata?.coins.toString() ?: "0",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    )
            }
        ) { paddingValues ->
            mainContent(paddingValues)
        }
    }
}