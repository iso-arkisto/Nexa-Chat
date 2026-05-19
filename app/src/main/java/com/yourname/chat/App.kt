package com.yourname.chat

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.yourname.chat.data.local.MainDb
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App: Application() {

}