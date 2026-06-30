package com.yourname.chat.presentation.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    class ResourceString(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    @Composable
    fun asString(): String {
        return when(this) {
            is DynamicString -> value
            is ResourceString -> stringResource(resId, *args.toTypedArray())
        }
    }
}