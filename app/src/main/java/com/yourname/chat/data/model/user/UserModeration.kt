package com.yourname.chat.data.model.user

data class UserModeration(
    val banInfo: BanData = BanData(),
    val reportsCount: Int = 0,
    val msgLimitsPerDay: Int = 600,
    val chatAccess: BanData = BanData(),
    val deletionAtMonths: Int = 12
)
