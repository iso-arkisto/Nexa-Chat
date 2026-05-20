package com.yourname.chat.domain.repository

import com.yourname.chat.data.local.MessageEntity
import com.yourname.chat.data.model.message.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    val currentUserId: String?

    suspend fun findMessageById(id: String): MessageEntity?
    suspend fun insertItem(message: MessageEntity)
    fun getMessages(chatId: String): Flow<List<Message>>
    fun getLastMessage(chatId: String): Flow<Message?>
    suspend fun updateMessage(item: MessageEntity, chatId: String): Result<Unit>
    suspend fun sendMessage(text: String, chatId: String): Result<Unit>
    suspend fun deleteMessages(messages: List<Message>): Result<Unit>
    suspend fun decryptMessage(message: Message): Result<String>

}