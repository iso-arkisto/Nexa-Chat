package com.yourname.chat.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {

    fun formatShort(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        if(diff < 0) {
            val futureDiff = -diff
            return when {
                futureDiff < 60_000 -> "in ${futureDiff/1000} s."
                futureDiff < 3600_000 -> "in ${futureDiff/60_000} min."
                futureDiff < 86_400_000 -> "in ${futureDiff/3600_000} h."
                futureDiff < 604_800_000 -> "in ${futureDiff/86_400_000} d."
                else -> formatDate(timestamp)
            }
        }

        return when {
            diff < 60_000 -> "just now"
            diff < 3600_000 -> "${diff/60_000} min. ago"
            diff < 86_400_000 -> "${diff/3600_000} h. ago"
            diff < 604_800_000 -> "${diff/86_400_000} d. ago"
            else -> formatDate(timestamp)
        }
    }

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

}

fun Long.toShortTimeString(): String = TimeFormatter.formatShort(this)
fun Long.toFullDateString(): String = TimeFormatter.formatDate(this)