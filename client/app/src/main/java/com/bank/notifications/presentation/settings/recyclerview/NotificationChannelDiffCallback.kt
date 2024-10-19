package com.bank.notifications.presentation.settings.recyclerview

import androidx.recyclerview.widget.DiffUtil
import com.bank.notifications.presentation.settings.NotificationSettingsState

class NotificationChannelDiffCallback :
    DiffUtil.ItemCallback<NotificationSettingsState.ChannelDataUi>() {
    override fun areItemsTheSame(
        oldItem: NotificationSettingsState.ChannelDataUi,
        newItem: NotificationSettingsState.ChannelDataUi
    ): Boolean {
        return oldItem.channelId == newItem.channelId
    }

    override fun areContentsTheSame(
        oldItem: NotificationSettingsState.ChannelDataUi,
        newItem: NotificationSettingsState.ChannelDataUi
    ): Boolean {
        return oldItem == newItem
    }
}