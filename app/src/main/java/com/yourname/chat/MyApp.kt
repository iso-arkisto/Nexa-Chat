package com.yourname.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.yourname.chat.data.model.user.User
import com.yourname.chat.presentation.viewmodel.UsersViewModel
import com.yourname.chat.navigation.NavGraph
import com.yourname.chat.utils.Screen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    val uvm: UsersViewModel = hiltViewModel()

    val auth = Firebase.auth
    var startDestination: String? by remember { mutableStateOf(null) }
    val remoteConfig = Firebase.remoteConfig
    val lifecycleOwner = LocalLifecycleOwner.current

    val uid = auth.currentUser?.uid


    LaunchedEffect(Unit) {

        var isWorking = true
        var isUpdated = true

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            isWorking = remoteConfig.getBoolean("working_condition")
            isUpdated = remoteConfig.getString("latest_version_name") == BuildConfig.VERSION_NAME

            if(!isWorking || !isUpdated) {

                val reason = if(!isWorking) {
                    "not_working"
                } else if(!isUpdated) {
                    "old_version_${remoteConfig.getString("latest_version_name")}"
                } else {
                    uid
                }

                startDestination = "${Screen.Ban}/${reason}"

            } else {
                startDestination = if(uid != null) {
                    Screen.ChatList
                } else {
                    Screen.Auth
                }
            }
        }

        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {

                remoteConfig.activate().addOnCompleteListener {
                    isWorking = remoteConfig.getBoolean("working_condition")
                    isUpdated = remoteConfig.getString("latest_version_name") == BuildConfig.VERSION_NAME

                    if(!isWorking || !isUpdated) {
                        val reason = if(!isWorking) {
                            "not_working"
                        } else if(!isUpdated) {
                            "old_version_${remoteConfig.getString("latest_version_name")}"
                        } else {
                            uid
                        }

                        startDestination = "${Screen.Ban}/${reason}"

                    } else {
                        startDestination = if(uid != null) {
                            Screen.ChatList
                        } else {
                            Screen.Auth
                        }
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.d("RemoteConfig","Remote config error: $error")
            }
        })

        if(uid!=null) {

            val db = Firebase.firestore

            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)

                    if(user!= null) {
                        if(user.moderation.banInfo.banned) {
                            startDestination = "${Screen.Ban}/${user.core.uid}"
                        }
                    }
                }

        }

        uvm.navigationEvent
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { banData ->
                val hasGraph = try {
                    navController.graph; true
                } catch (e: IllegalStateException) {
                    false
                }

                if(hasGraph && navController.currentDestination?.route != Screen.Ban) {
                    navController.navigate("${Screen.Ban}/${uid}") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
    }

    NavGraph(
        navController = navController,
        startDestination = startDestination
    )
}