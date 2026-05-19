package com.yourname.chat.domain

import com.yourname.chat.data.model.user.PrivacySetting
import com.yourname.chat.data.model.user.User
import javax.inject.Inject

class CheckUserAccessUseCase @Inject constructor() {
    operator fun invoke(
        user1: User,
        user2: User,
        permissionType: String
    ): Boolean {
        val isBlocked = user2.core.uid in user1.social.blockedUsers ||
                user1.core.uid in user2.social.blockedUsers

        if (isBlocked) return false

        return when (permissionType.uppercase()) {
            PrivacySetting.EVERYONE.name -> true
            PrivacySetting.FRIENDS.name -> user2.core.uid in user1.social.friends || user1.core.uid in user2.social.friends
            PrivacySetting.CLOSE_FRIENDS.name -> user2.core.uid in user1.social.closeFriends || user1.core.uid in user2.social.closeFriends
            else -> false
        }
    }
}
