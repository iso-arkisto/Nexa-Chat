package com.yourname.chat.data.model.chat

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// subcollections: members, admins, blacklist, entryrequests
data class Chat(
    val id: String = "",
    val name: String = "New chat",
    val avatar: String? = null,
    val description: String = "",
    val link: String? = null,
    val permissions: Int = 0,
    val slowModeMs: Long = 1000,
    val pinnedMessages: List<String> = emptyList(), // max 10
    val disappearingMessagesDurationMs: Long? = null,
    val passCodeHash: String? = null,
    val oneTimeInvitationLinks: List<String> = emptyList(), // max 30

    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    fun hasPermission(permission: Int): Boolean = (permissions and permission) != 0
}