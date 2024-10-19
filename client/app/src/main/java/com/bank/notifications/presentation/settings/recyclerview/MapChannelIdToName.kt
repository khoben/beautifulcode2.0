package com.bank.notifications.presentation.settings.recyclerview

import com.bank.notifications.R
import com.bank.notifications.common.platform.PlatformString
import java.util.Locale

interface MapChannelIdToName {
    fun map(channelId: String): PlatformString

    class ResourceOrDefault(private val map: Map<String, Int> = DEFAULT_MAP) : MapChannelIdToName {

        override fun map(channelId: String): PlatformString {
            val resourceStringId = map[channelId]
            if (resourceStringId != null) {
                return PlatformString.Resource(resourceStringId)
            }
            return PlatformString.Plain(channelId.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            })
        }

        companion object {
            private val DEFAULT_MAP = mapOf(
                "transactions" to R.string.channel_transactions,
                "deposits" to R.string.channel_deposits,
                "withdrawals" to R.string.channel_withdrawals,
                "confirmations" to R.string.channel_confirmations,
                "promotions" to R.string.channel_promotions,
                "chat" to R.string.channel_chat,
                "support" to R.string.channel_support
            )
        }
    }
}