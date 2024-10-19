package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.NotificationChannelRequest
import com.bank.notifications.domain.model.NotificationChannelDomainRequest

class NotificationChannelDomainRequestMapper :
    Mapper<NotificationChannelDomainRequest, NotificationChannelRequest> {
    override fun map(data: NotificationChannelDomainRequest): MapperResult<NotificationChannelRequest> {
        return MapperResult.Success(
            NotificationChannelRequest(
                smsEnabled = data.smsEnabled,
                emailEnabled = data.emailEnabled,
                pushEnabled = data.pushEnabled
            )
        )
    }
}