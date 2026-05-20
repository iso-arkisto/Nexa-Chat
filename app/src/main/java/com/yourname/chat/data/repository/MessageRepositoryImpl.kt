package com.yourname.chat.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yourname.chat.data.local.MessageDao
import com.yourname.chat.data.local.MessageEntity
import com.yourname.chat.data.model.message.Message
import com.yourname.chat.data.model.user.User
import com.yourname.chat.domain.repository.MessageRepository
import com.yourname.chat.data.manager.CryptoManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val dao: MessageDao,
    private val firestore: FirebaseFirestore,
    private val cryptoManager: CryptoManager,
    private val auth: FirebaseAuth
): MessageRepository {
    override val currentUserId: String?
        get() = auth.currentUser?.uid
    override suspend fun findMessageById(id: String): MessageEntity? {
        return dao.findMessageById(id)
    }

    override suspend fun insertItem(message: MessageEntity) {
        dao.insertItem(message)
    }

    override fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val currentUid = auth.currentUser?.uid ?: return@callbackFlow

        val query = firestore.collection("messages")
            .where(
                if(currentUid == chatId) {
                    // storage messages
                    Filter.and(
                        Filter.equalTo("chatId", chatId),
                        Filter.equalTo("senderId", chatId)
                    )
                } else {
                    // covers both directions of dialogue
                    Filter.or(
                        Filter.and(
                            Filter.equalTo("chatId", chatId),
                            Filter.equalTo("senderId", currentUid)
                        ),
                        Filter.and(
                            Filter.equalTo("chatId", currentUid),
                            Filter.equalTo("senderId", chatId)
                        )
                    )
                }
            )
            .orderBy("timestamp",Query.Direction.ASCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
                if(error!=null) {
                    close(error)
                    return@addSnapshotListener
                }

                if(snapshot != null) {
                    val allMessages = snapshot.toObjects(Message::class.java)
                    trySend(allMessages)
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getLastMessage(chatId: String): Flow<Message?> = callbackFlow {
        val currentUid = auth.currentUser?.uid ?: return@callbackFlow

        val query = firestore.collection("messages")
            .where(
                if(currentUid == chatId) {
                    Filter.and(
                        Filter.equalTo("chatId", chatId),
                        Filter.equalTo("senderId", chatId)
                    )
                } else {
                    Filter.or(
                        Filter.and(
                            Filter.equalTo("chatId", chatId),
                            Filter.equalTo("senderId", currentUid)
                        ),
                        Filter.and(
                            Filter.equalTo("chatId", currentUid),
                            Filter.equalTo("senderId", chatId)
                        )
                    )
                }
            )
            .orderBy("timestamp",Query.Direction.DESCENDING)
            .limit(1)

        val listener = query.addSnapshotListener { snapshots, error ->
            if(error != null) {
                close(error)
                return@addSnapshotListener
            }

            val message = snapshots?.documents?.firstOrNull()?.toObject(Message::class.java)
            trySend(message)
        }

        awaitClose { listener.remove() }
    }


    override suspend fun updateMessage(item: MessageEntity, chatId: String): Result<Unit> {
       return runCatching {
            val document = firestore.collection("users")
                .document(chatId)
                .get()
                .await()

            val receiverPublicKey = document.getString("core.publicKey") ?:
            throw NoSuchElementException("Receiver public key not found")

            if(item.text != null) {
                val encryptedText = cryptoManager.encrypt(item.text, receiverPublicKey)
                insertItem(item)

                val messageData = mapOf(
                    ("text" to encryptedText),
                    ("edited" to FieldValue.serverTimestamp())
                )

                firestore.collection("messages")
                    .document(item.id)
                    .update(messageData)
                    .await()
            }
        }.onFailure { e ->
            Log.e("MessageRepository", "Failed to edit message", e)
            throw e
        }
    }

    override suspend fun sendMessage(text: String, chatId: String): Result<Unit> {
        return runCatching {
            val currentUid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

            val document = firestore.collection("users")
                .document(chatId)
                .get()
                .await()

            val receiverPublicKey = document.toObject(User::class.java)?.core?.publicKey ?:
            throw NoSuchElementException("Public key not found")

                val messageID = UUID.randomUUID().toString()
                val encryptedText = cryptoManager.encrypt(text, receiverPublicKey)
                insertItem(MessageEntity(messageID, text))

                val messageData = Message(
                    id = messageID,
                    senderId = currentUid,
                    chatId = chatId,
                    text = encryptedText
                )

                firestore.collection("messages")
                    .document(messageID)
                    .set(messageData)
                    .await()

            Unit
        }.onFailure { e ->
            Log.e("MessageRepository", "Failed to send message", e)
            throw e
        }
    }

    override suspend fun deleteMessages(messages: List<Message>): Result<Unit> {
        return runCatching {
            if (messages.isEmpty()) {
                return@runCatching
            }

            val batch = firestore.batch()

            messages.forEach { msg ->
                val docRef = firestore.collection("messages").document(msg.id)
                batch.delete(docRef)
            }

            batch.commit().await()
        }.onFailure { e ->
            Log.e("MessageRepository", "Failed to delete messages", e)
        }
    }

    override suspend fun decryptMessage(message: Message): Result<String> = runCatching {
        val text = message.text

        val currentUid = auth.currentUser?.uid ?: throw java.lang.IllegalStateException("User not authorized")

        val isMyMessage = message.senderId == currentUid

        if(text.isNullOrBlank()) {
            ""
        } else {
            if(isMyMessage) {
                findMessageById(message.id)?.text ?: text
            } else {
                val keysetHandle =  cryptoManager.getPrivateKeyHandle()
                cryptoManager.decrypt(text, keysetHandle)
            }
        }
    }.onFailure { e ->
        Log.e("MessageRepository", "Failed to decrypt message", e)
    }
}