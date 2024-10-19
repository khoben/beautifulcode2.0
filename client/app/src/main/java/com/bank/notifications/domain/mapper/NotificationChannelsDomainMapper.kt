package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.NotificationChannelsResponse
import com.bank.notifications.domain.model.NotificationChannelDomain
import com.bank.notifications.domain.model.NotificationChannelsDomain

class NotificationChannelsDomainMapper :
    Mapper<NotificationChannelsResponse, NotificationChannelsDomain> {
    override fun map(data: NotificationChannelsResponse): MapperResult<NotificationChannelsDomain> {
        return when {
            data.notificationChannels == null -> MapperResult.InvalidDataError()
            else -> {
                val domainChannels =
                    ArrayList<NotificationChannelDomain>(data.notificationChannels.size)
                for (channel in data.notificationChannels) {
                    if (channel.channelId == null || channel.smsEnabled == null ||
                        channel.emailEnabled == null || channel.pushEnabled == null
                    ) {
                        return MapperResult.InvalidDataError()
                    }
                    domainChannels.add(
                        NotificationChannelDomain(
                            channelId = channel.channelId,
                            smsEnabled = channel.smsEnabled,
                            emailEnabled = channel.emailEnabled,
                            pushEnabled = channel.pushEnabled
                        )
                    )
                }
                MapperResult.Success(NotificationChannelsDomain(domainChannels))
            }
        }
    }
}