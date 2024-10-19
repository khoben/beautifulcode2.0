package com.bank.notifications.domain.model

data class NotificationChannelDomain(
    val channelId: String,
    val smsEnabled: Boolean,
    val emailEnabled: Boolean,
    val pushEnabled: Boolean
)