package com.bank.notifications.data.cloud

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationChannelsResponse(
    @SerialName("notification_channels")
    val notificationChannels: List<NotificationChannelResponse>? = null
)
