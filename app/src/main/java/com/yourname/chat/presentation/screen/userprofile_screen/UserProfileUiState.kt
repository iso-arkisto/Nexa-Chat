package com.yourname.chat.presentation.screen.userprofile_screen

import com.yourname.chat.data.model.user.PrivacySetting
import com.yourname.chat.data.model.user.User
import com.yourname.chat.data.model.user.UserStatus
import com.yourname.chat.presentation.components.UiText

sealed interface UserProfileUiState {
    data object Loading : UserProfileUiState

    data class Error(val message: UiText) : UserProfileUiState

    data class Success(
        val targetUser: User,
        val currentUser: User,
        val userStatus: UserStatus,
        val friendButtonState: FriendButtonState,
        private val checkAccess: (User, User, String) -> Boolean
    ) : UserProfileUiState {

        val canSeeEmail = checkAccess(targetUser, currentUser, targetUser.privacy.privacyEmail)
        val canSeePhone = checkAccess(targetUser, currentUser, targetUser.privacy.privacyPhoneNumber)
                && targetUser.core.phoneNumber != null
        val isProfileInfoVisible: Boolean by lazy {
            val hasStatus = targetUser.core.customStatus != null
            val hasGeo = userStatus.geo != null
            val hasBio = targetUser.core.bio.isNotBlank()

            hasStatus || hasGeo || hasBio || canSeeEmail || canSeePhone
        }
    }
}