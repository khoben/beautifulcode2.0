package com.bank.notifications.data.cloud

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationChannelResponse(
    @SerialName("channel_id")
    val channelId: String? = null,
    @SerialName("sms_enabled")
    val smsEnabled: Boolean? = null,
    @SerialName("email_enabled")
    val emailEnabled: Boolean? = null,
    @SerialName("push_enabled")
    val pushEnabled: Boolean? = null,
)
