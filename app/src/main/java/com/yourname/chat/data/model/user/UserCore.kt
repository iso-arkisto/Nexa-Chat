package com.yourname.chat.data.model.user

data class UserCore(
    val uid: String = "",
    val username: String? = null,
    val displayName: String = "User${(10000..99999).random()}",
    val photoUrl: String? = null,
    val bio: String = "",
    val phoneNumber: String? = null,
    val email: String = "",
    val isPremium: Boolean = false,
    val verified: Boolean = false,
    val languageCode: String = "en",
    val customStatus: String? = null,
    val publicKey: String? = null
)
