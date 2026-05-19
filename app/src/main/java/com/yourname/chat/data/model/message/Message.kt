package com.yourname.chat.data.model.message

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

// subcollections: reactions
data class Message(

    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val chatId: String = "",

    @ServerTimestamp
    val timestamp: Timestamp? = null,

    val pollId: String? = null,
    val replyId: String? = null,
    val text: String? = null,
    val media: List<String> = emptyList(),
    val sticker: String? = null,
    val geo: GeoPoint? = null,
    val contacts: List<String> = emptyList(),

    val deliveryStatus: String = DeliveryStatus.SENT.name,

    val edited: Timestamp? = null,

    val deletionTimerSeconds: Int? = null,

    val mentions: List<String> = emptyList(),
    val forwardedMessageId: String? = null
)

enum class DeliveryStatus {
    SENT,
    READ,
    DELIVERED,
    FAILED,
    PENDING
}

data class Reaction(
    val userId: String,
    val emoji: String,
    @ServerTimestamp val timestamp: Timestamp?
)