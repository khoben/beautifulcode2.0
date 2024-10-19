package com.bank.notifications.presentation.settings

import androidx.annotation.StringRes
import com.bank.notifications.domain.model.NotificationChannelDomain
import com.bank.notifications.domain.model.UserProfileDomain

interface NotificationSettingsState {
    data class UserDataUi(
        val userPhone: String,
        val userEmail: String,
    ) {
        companion object {
            val EMPTY = UserDataUi("", "")
            fun fromDomain(user: UserProfileDomain) = UserDataUi(
                userPhone = user.phone,
                userEmail = user.email,
            )
        }
    }

    data class ChannelDataUi(
        val channelId: String,
        val smsEnabled: Boolean,
        val emailEnabled: Boolean,
        val pushEnabled: Boolean,
    ) {
        companion object {
            fun fromDomain(channel: NotificationChannelDomain) = ChannelDataUi(
                channelId = channel.channelId,
                smsEnabled = channel.smsEnabled,
                emailEnabled = channel.emailEnabled,
                pushEnabled = channel.pushEnabled,
            )
        }
    }

    sealed class DataState {
        data class Loading(val isLoading: Boolean) : DataState()
        data class Data(val data: List<ChannelDataUi>) : DataState()
        data class Error(val error: Throwable? = null) : DataState()
        data object Empty : DataState()
    }

    data class UiState(val dataState: DataState) {
        companion object {
            val EMPTY = UiState(dataState = DataState.Empty)
        }
    }

    sealed class UiEffect {
        data object CheckPushEnabled : UiEffect()
        data class RefreshListItem(val itemId: Int) : UiEffect()
        data class ShowEditEmail(val email: String) : UiEffect()
        data class ShowEditPhone(val phone: String) : UiEffect()
        data class ShowToast(@StringRes val message: Int) : UiEffect()
    }
}