package com.bank.notifications.presentation.settings.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bank.notifications.databinding.RecyclerViewNotificationChannelSettingsItemBinding
import com.bank.notifications.presentation.settings.NotificationSettingsState

class NotificationListAdapter(
    private val onNotificationEnableChanged: (
        itemListIdx: Int,
        channelId: String,
        smsEnabled: Boolean,
        emailEnabled: Boolean,
        pushEnabled: Boolean
    ) -> Unit,
    private val mapChannelIdToName: MapChannelIdToName = MapChannelIdToName.ResourceOrDefault()
) : ListAdapter<NotificationSettingsState.ChannelDataUi, NotificationListAdapter.ViewHolder>(
    NotificationChannelDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RecyclerViewNotificationChannelSettingsItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ViewHolder(
        private val binding: RecyclerViewNotificationChannelSettingsItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationSettingsState.ChannelDataUi) {
            binding.notificationChannelName.text =
                mapChannelIdToName.map(item.channelId)
                    .string(binding.notificationChannelName.context)

            binding.checkSms.isChecked = item.smsEnabled
            binding.checkEmail.isChecked = item.emailEnabled
            binding.checkPush.isChecked = item.pushEnabled

            val itemListIdx = bindingAdapterPosition

            binding.checkSms.setOnClickListener {
                onNotificationEnableChanged(
                    itemListIdx,
                    item.channelId,
                    !item.smsEnabled,
                    item.emailEnabled,
                    item.pushEnabled
                )
            }

            binding.checkEmail.setOnClickListener {
                onNotificationEnableChanged(
                    itemListIdx,
                    item.channelId,
                    item.smsEnabled,
                    !item.emailEnabled,
                    item.pushEnabled
                )
            }
            binding.checkPush.setOnClickListener {
                onNotificationEnableChanged(
                    itemListIdx,
                    item.channelId,
                    item.smsEnabled,
                    item.emailEnabled,
                    !item.pushEnabled
                )
            }
        }
    }
}