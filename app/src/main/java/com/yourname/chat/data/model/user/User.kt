package com.yourname.chat.data.model.user

data class User (
    val core: UserCore = UserCore(),
    val privacy: UserPrivacy = UserPrivacy(),
    val social: UserSocial = UserSocial(),
    val moderation: UserModeration = UserModeration(),
    val metadata: UserMetadata = UserMetadata()
)