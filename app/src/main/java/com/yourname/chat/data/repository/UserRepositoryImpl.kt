package com.yourname.chat.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.yourname.chat.data.model.user.BanData
import com.yourname.chat.data.model.message.Message
import com.yourname.chat.data.model.user.User
import com.yourname.chat.data.model.user.UserStatus
import com.yourname.chat.presentation.screen.chatlist_screen.UserChatUiState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
): UserRepository {
    override val currentUserId: String?
        get() = auth.currentUser?.uid

    companion object {
        private const val MAX_FRIENDS = 200
        private const val MAX_PENDING_REQUESTS = 200
    }
    val currentUserFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun getCurrentUserBanStatus(): Flow<BanData?> = currentUserFlow
        .map { it?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
        uid?.let {
            callbackFlow {
                val listener = firestore
                    .collection("users")
                    .document(uid)
                    .addSnapshotListener { snapshot, error ->

                        if(error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        val banData = snapshot?.get("moderation.banInfo") as? BanData
                        trySend(banData)
                    }
                awaitClose { listener.remove() }
            }
        } ?: flowOf(null)
    }

    override fun observeBanInfo(userId: String): Flow<BanData?> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                val data = snapshot?.toObject(User::class.java)?.moderation?.banInfo
                trySend(data)
            }
        awaitClose { listener.remove() }
    }

    override fun getUserData(userId: String): Flow<User?> = callbackFlow {
        val docRef = firestore.collection("users").document(userId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if(error != null) {
                close(error)
                return@addSnapshotListener
            }

            val user = snapshot?.toObject(User::class.java)
            trySend(user)
        }

        awaitClose { listener.remove() }
    }

    override fun getCurrentUserData(): Flow<User?> = currentUserFlow
        .map { it?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            uid?.let {
                callbackFlow {
                    val listener = firestore
                        .collection("users")
                        .document(uid)
                        .addSnapshotListener { snapshot, error ->

                            if(error != null) {
                                close(error)
                                return@addSnapshotListener
                            }

                            val user = snapshot?.toObject(User::class.java)

                            trySend(user)
                        }
                    awaitClose { listener.remove() }
                }
            } ?: flowOf(null)
        }

    override fun getUserStatus(userId: String): Flow<UserStatus?> = callbackFlow {
        val userStatusRef = database.getReference("/status/${userId}")
        var lastValue: UserStatus? = null

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val state = if(snapshot.exists()) {
                    snapshot.getValue(UserStatus::class.java)
                } else {
                    null
                }

                if(state != lastValue) {
                    lastValue = state
                    trySend(state)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserRepository", "Error getting user status for $userId", error.toException())
                close(error.toException())
            }
        }

        userStatusRef.addValueEventListener(listener)

        awaitClose { userStatusRef.removeEventListener(listener) }
    }

    override suspend fun removeFriend(currentId: String, targetId: String): Result<Unit> = runCatching {
        firestore.runBatch { batch ->
            val user1Ref = firestore.collection("users").document(currentId)
            val user2Ref = firestore.collection("users").document(targetId)

            batch.update(user1Ref, "social.friends", FieldValue.arrayRemove(targetId))
            batch.update(user2Ref, "social.friends", FieldValue.arrayRemove(currentId))
        }.await()
    }

    override suspend fun acceptFriendRequest(currentUser: User, targetUser: User): Result<Unit> = runCatching {
        if(currentUser.social.friends.size >= MAX_FRIENDS || targetUser.social.friends.size >= MAX_FRIENDS) {
            throw IllegalStateException("Friend limit reached")
        }

        firestore.runBatch { batch ->
            val user1Ref = firestore.collection("users").document(currentUser.core.uid)
            val user2Ref = firestore.collection("users").document(targetUser.core.uid)

            batch.update(user1Ref, "social.pendingFriendshipRequests", FieldValue.arrayRemove(targetUser.core.uid))
            batch.update(user1Ref, "social.friends", FieldValue.arrayUnion(targetUser.core.uid))
            batch.update(user2Ref, "social.friends", FieldValue.arrayUnion(currentUser.core.uid))
        }.await()
    }

    override suspend fun sendFriendRequest(currentId: String, targetUser: User): Result<Unit> = runCatching {
        if (targetUser.social.pendingFriendshipRequests.size >= MAX_PENDING_REQUESTS) {
            throw IllegalStateException("Too many pending requests")
        }

        firestore.collection("users").document(targetUser.core.uid)
            .update("social.pendingFriendshipRequests", FieldValue.arrayUnion(currentId))
            .await()
    }

    override suspend fun addFriend(currentUser: User?, targetUser: User?): Result<Unit> = runCatching {
            if(currentUser == null || targetUser == null) return@runCatching

            val currentId = currentUser.core.uid
            val targetId = targetUser.core.uid
            if(currentId == targetId) throw IllegalStateException("You can't be your own friend")

            when {
                currentUser.social.friends.contains(targetId) -> {
                    removeFriend(currentId, targetId).getOrThrow()
                }
                currentUser.social.pendingFriendshipRequests.contains(targetId) -> {
                    acceptFriendRequest(currentUser, targetUser).getOrThrow()
                }
                else -> {
                    sendFriendRequest(currentId, targetUser).getOrThrow()
                }
            }
    }.onFailure { exception ->
        Log.e("UserRepository", "Failed to add friend for user", exception)
    }

    override fun getAllUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if(error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if(snapshot != null) {
                    val users = snapshot.toObjects(User::class.java)
                    trySend(users)
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getConnectedUsers(): Flow<List<String>> = callbackFlow {
        val listener = firestore
            .collection("messages")
            .where(
                Filter.or(
                    Filter.equalTo("chatId", currentUserId),
                    Filter.equalTo("senderId", currentUserId)
                )
            )

            .addSnapshotListener { snapshot, error ->
                if(error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if(snapshot != null) {
                   val messages = snapshot.toObjects(Message::class.java).sortedBy { it.timestamp }

                    val usersIds = messages.mapNotNull { msg ->
                        when(currentUserId) {
                            msg.chatId -> msg.senderId
                            msg.senderId -> msg.chatId
                            else -> null
                        }
                    }.distinct()

                    trySend(usersIds)
                }
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getCurrentUser(): Result<User?> {
        return runCatching {

            val authId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

            val snapshot = firestore.collection("users")
                .document(authId)
                .get()
                .await()

            if(!snapshot.exists()) {
                throw NoSuchElementException("User document not found")
            }

            snapshot.toObject(User::class.java)
        }.onFailure { error ->
            Log.e("UserRepository","Failed to get current user", error)
        }
    }

    override suspend fun getUserByUid(userId: String): Result<User?> {
        return runCatching {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if(!snapshot.exists()) {
                throw NoSuchElementException("User document not found")
            }

            snapshot.toObject(User::class.java)
        }.onFailure { error ->
            Log.e("UserRepository","Failed to get user by id", error)
        }
    }

    override suspend fun logOut() {
        val userStatusRef = database.getReference("/status/${auth.currentUser?.uid}")

        userStatusRef.setValue(mapOf(
            "state" to "offline",
            "lastSeen" to ServerValue.TIMESTAMP,
            "typing" to null,
            "geo" to null
        ))

        auth.signOut()
    }

    override suspend fun updateUserTyping(value: String?): Result<Unit> {
        return runCatching {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
            val userRef = database.getReference("/status/$userId")
            val updates = mapOf("typing" to value)

            userRef.updateChildren(updates).await()
            Unit

        }.onFailure { e ->
            Log.e("UserRepository", "Failed to update user typing status", e)
        }
    }

    override suspend fun unbanUser(): Result<Unit> = runCatching {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        firestore.collection("users")
            .document(userId)
            .update(hashMapOf<String, Any>(
              "moderation.banInfo.banned" to false
            ))
        Unit
    }.onFailure { error ->
        Log.e("UserRepository", "Failed to unban user", error)
    }

    override suspend fun checkUsernameAvailability(username: String): Result<Boolean> = runCatching {
        val snapshot = firestore.collection("users")
            .whereEqualTo("core.username", username)
            .limit(1)
            .get()
            .await()

        val doc = snapshot.documents.firstOrNull()

        if(doc != null) {
            if(doc.id == currentUserId) {
                true
            } else {
                false
            }
        } else {
            true
        }
    }.onFailure { error ->
        Log.e("UserRepository", "Failed to check username availability", error)
    }

    override suspend fun updateCurrentUserData(data: HashMap<String, Any?>): Result<Unit> = runCatching {
        val authId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        firestore.collection("users")
            .document(authId)
            .update(data)

        Unit
    }.onFailure { error ->
        Log.e("UserRepository", "Failed to update current account data", error)
    }

    override suspend fun updateCurrentUserStatus(data: Map<String, Any?>): Result<Unit> = runCatching {
        val authId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val userStatusRef = database.getReference("/status/${authId}")
        userStatusRef.setValue(data)
        Unit
    }.onFailure { error ->
        Log.e("UserRepository", "Failed to update current account status", error)
    }
}