package com.yourname.chat.data.model.user

data class UserPrivacy(
    val privacyLastSeen: String = PrivacySetting.FRIENDS.name,
    val privacyOnline: String = PrivacySetting.EVERYONE.name,
    val privacyPhoneNumber: String = PrivacySetting.CONTACTS.name,
    val privacyEmail: String = PrivacySetting.NOBODY.name,
    val privacyPhoto: String = PrivacySetting.EVERYONE.name,
    val canBeAddedToGroups: String = PrivacySetting.FRIENDS.name,
    val whoCanChat: String = PrivacySetting.EVERYONE.name,
    val privacyGeo: String = PrivacySetting.CLOSE_FRIENDS.name,
    val privacyInviteRoom: String = PrivacySetting.FRIENDS.name,
    val privacySearch: Boolean = true
)
