package com.yourname.chat.data.model.message

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

// subcolletions: poll_votes
data class Poll(
    val id: String = UUID.randomUUID().toString(),
    val question: String = "",
    val options: List<String>,
    val explanation: String? = null,
    val isAnonymous: Boolean = false,
    val allowMultipleAnswers: Boolean = false,
    val type: String = "REGULAR",
    val correctOption: Int? = null, // index
    val isClosed: Boolean = false,
    val closeDate: Timestamp? = null,
)

data class PollVote(
    val userId: String,
    val selectedOptions: List<Int>,
    @ServerTimestamp val timestamp: Timestamp?
)