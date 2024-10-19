package com.bank.notifications.domain.model

data class UserProfileDomainRequest(
    val email: String? = null,
    val phone: String? = null
)