package com.bank.notifications.data.cloud

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    @SerialName("email")
    val email: String? = null,
    @SerialName("phone")
    val phone: String? = null
)