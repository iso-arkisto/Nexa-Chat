package com.yourname.chat.data.model.user

data class UserSocial(
    val closeFriends: List<String> = emptyList(),
    val pendingFriendshipRequests: List<String> = emptyList(),
    val friends: List<String> = emptyList(),
    val blockedUsers: List<String> = emptyList(),
    val pinnedChats: List<String> = emptyList()
)
