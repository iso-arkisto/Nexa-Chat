package com.yourname.chat.data.model.user

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

data class UserStatus(
    val state: String = "offline",

    @ServerTimestamp
    val lastSeen: Long? = null,
    val typing: String? = "",
    val geo: GeoPoint? = null
)
