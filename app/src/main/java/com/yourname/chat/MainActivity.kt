package com.yourname.chat

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.yourname.chat.data.remote.UserPresenceManager
import com.yourname.chat.utils.ServerTimeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var presenceManager: UserPresenceManager
    @Inject
    lateinit var serverTimeManager: ServerTimeManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenceManager.startTracking()
        serverTimeManager.startSync()
        setContent {
            MyApp()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        presenceManager.stopTracking()
    }
}

