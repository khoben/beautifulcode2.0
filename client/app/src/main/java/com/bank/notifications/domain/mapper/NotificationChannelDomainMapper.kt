package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.NotificationChannelResponse
import com.bank.notifications.domain.model.NotificationChannelDomain

class NotificationChannelDomainMapper :
    Mapper<NotificationChannelResponse, NotificationChannelDomain> {
    override fun map(data: NotificationChannelResponse): MapperResult<NotificationChannelDomain> =
        when {
            data.channelId == null ||
                    data.smsEnabled == null ||
                    data.emailEnabled == null ||
                    data.pushEnabled == null -> MapperResult.InvalidDataError()

            else -> MapperResult.Success(
                NotificationChannelDomain(
                    channelId = data.channelId,
                    smsEnabled = data.smsEnabled,
                    emailEnabled = data.emailEnabled,
                    pushEnabled = data.pushEnabled
                )
            )
        }
}