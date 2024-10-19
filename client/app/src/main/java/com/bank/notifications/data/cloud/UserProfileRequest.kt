package com.bank.notifications.data.cloud

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileRequest(
    @SerialName("email")
    val email: String?,
    @SerialName("phone")
    val phone: String?
)