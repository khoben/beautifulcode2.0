package com.bank.notifications.domain.usecase

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.common.mapper.getOrThrow
import com.bank.notifications.data.cloud.NotificationChannelRequest
import com.bank.notifications.data.cloud.NotificationChannelResponse
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.data.error.HttpResponseInvalidDataException
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.mapper.NotificationChannelDomainMapper
import com.bank.notifications.domain.mapper.NotificationChannelDomainRequestMapper
import com.bank.notifications.domain.model.NotificationChannelDomain
import com.bank.notifications.domain.model.NotificationChannelDomainRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class UpdateUserNotificationChannel(
    private val service: NotificationHttpService,
    private val inMapper: Mapper<NotificationChannelDomainRequest, NotificationChannelRequest> = NotificationChannelDomainRequestMapper(),
    private val outMapper: Mapper<NotificationChannelResponse, NotificationChannelDomain> = NotificationChannelDomainMapper()
) : UseCaseParam1<NotificationChannelDomain, NotificationChannelDomainRequest> {
    override suspend fun invoke(input: NotificationChannelDomainRequest): Flow<UseCaseResult<NotificationChannelDomain, HttpException>> {
        return flow {
            val result = service.upsertUserNotificationChannel(
                input.channelId, inMapper.map(input).getOrThrow()
            )
            if (result.isSuccessful) {
                val notificationChannel = result.body()!!
                when (val mapperResult = outMapper.map(notificationChannel)) {
                    is MapperResult.Success -> emit(UseCaseResult.Success(mapperResult.data))
                    is MapperResult.InvalidDataError -> emit(
                        UseCaseResult.Error(
                            HttpResponseInvalidDataException(
                                response = result
                            )
                        )
                    )
                }
            } else {
                emit(UseCaseResult.Error(HttpException(result)))
            }
        }

    }
}