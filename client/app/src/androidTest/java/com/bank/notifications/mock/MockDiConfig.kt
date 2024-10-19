package com.bank.notifications.mock

data class MockDiConfig(
    val authSuccess: Boolean = true,
    val getUserProfileSuccess: Boolean = true,
    val postUserProfileSuccess: Boolean = true,
    val getNotificationChannelsSuccess: Boolean = true,
    val getNotificationChannelSuccess: Boolean = true,
    val postNotificationChannelSuccess: Boolean = true,

    val userPhone: String = "1234567890",
    val userEmail: String = "test@test.com",

    val channelId: String = "transactions",
    val channelSmsEnabled: Boolean = true,
    val channelEmailEnabled: Boolean = true,
    val channelPushEnabled: Boolean = true,
)
