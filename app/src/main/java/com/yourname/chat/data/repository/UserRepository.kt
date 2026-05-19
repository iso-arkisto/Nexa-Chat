package com.yourname.chat.data.repository

import com.yourname.chat.data.model.user.BanData
import com.yourname.chat.data.model.user.User
import com.yourname.chat.data.model.user.UserStatus
import com.yourname.chat.presentation.screen.chatlist_screen.UserChatUiState
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val currentUserId: String?
    fun observeBanInfo(userId: String): Flow<BanData?>
    fun getCurrentUserBanStatus(): Flow<BanData?>
    fun getUserData(userId: String): Flow<User?>
    fun getCurrentUserData(): Flow<User?>
    fun getUserStatus(userId: String): Flow<UserStatus?>
    suspend fun addFriend(currentUser: User?, targetUser: User?): Result<Unit>
    fun getAllUsersFlow(): Flow<List<User>>
    fun getConnectedUsers(): Flow<List<String>>
    suspend fun getCurrentUser(): Result<User?>
    suspend fun getUserByUid(userId: String): Result<User?>
    suspend fun logOut()
    suspend fun updateUserTyping(value: String?): Result<Unit>
    suspend fun unbanUser(): Result<Unit>
    suspend fun removeFriend(currentId: String, targetId: String): Result<Unit>
    suspend fun acceptFriendRequest(currentUser: User, targetUser: User): Result<Unit>
    suspend fun sendFriendRequest(currentId: String, targetUser: User): Result<Unit>
    suspend fun checkUsernameAvailability(username: String): Result<Boolean>
    suspend fun updateCurrentUserData(data: HashMap<String, Any?>): Result<Unit>
    suspend fun updateCurrentUserStatus(data: Map<String, Any?>): Result<Unit>
}