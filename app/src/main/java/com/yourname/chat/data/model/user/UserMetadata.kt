package com.yourname.chat.data.model.user

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import com.yourname.chat.BuildConfig

data class UserMetadata(
    val fcmToken: String? = null,
    val coins: Int = 0,

    @ServerTimestamp
    val createdAt: Timestamp? = null,

    val appVersion: String = BuildConfig.VERSION_NAME
)
