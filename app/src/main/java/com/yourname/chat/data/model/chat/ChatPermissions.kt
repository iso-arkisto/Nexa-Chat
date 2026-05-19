package com.yourname.chat.data.model.chat

// Bitmask
object ChatPermissions {
    const val ENTRY_REQUESTS = 1 shl 0
    const val IS_PUBLIC = 1 shl 1
    const val HIDE_MEMBERS = 1 shl 2
    const val SCREENSHOT_PROTECTION = 1 shl 3
    const val BLOCK_FORWARDING = 1 shl 4
    const val HIDE_OLD_MESSAGES = 1 shl 5
    const val ONLY_ADMINS_CHAT = 1 shl 6
}