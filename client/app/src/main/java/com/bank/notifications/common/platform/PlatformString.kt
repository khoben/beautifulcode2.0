package com.bank.notifications.common.platform

import android.content.Context
import androidx.annotation.StringRes

sealed class PlatformString {
    abstract fun string(context: Context): String

    class Plain(private val text: String) : PlatformString() {
        override fun string(context: Context) = text

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Plain) return false
            if (text != other.text) return false
            return true
        }

        override fun hashCode(): Int = text.hashCode()
    }

    class Resource(@StringRes private val resId: Int) : PlatformString() {
        override fun string(context: Context) = context.getString(resId)
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Resource) return false
            if (resId != other.resId) return false
            return true
        }

        override fun hashCode(): Int = resId
    }

    class Arguments(
        @StringRes private val resId: Int,
        private vararg val args: Any
    ) : PlatformString() {
        override fun string(context: Context) =
            context.getString(
                resId,
                *args.map { if (it is PlatformString) it.string(context) else it }.toTypedArray()
            )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Arguments) return false
            if (resId != other.resId) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }

        override fun hashCode(): Int = 31 * resId + args.contentHashCode()
    }
}