package com.yourname.chat.data.model.user

data class BanData(
    val banned: Boolean = false,
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val reason: String? = null
)
