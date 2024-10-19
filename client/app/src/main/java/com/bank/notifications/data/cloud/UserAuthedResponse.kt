package com.bank.notifications.data.cloud

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAuthedResponse(
    @SerialName("token")
    val token: String? = null
)
