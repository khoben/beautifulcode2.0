package com.bank.notifications.data.cloud

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationChannelRequest(
    @SerialName("sms_enabled")
    val smsEnabled: Boolean,
    @SerialName("email_enabled")
    val emailEnabled: Boolean,
    @SerialName("push_enabled")
    val pushEnabled: Boolean,
)