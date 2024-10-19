package com.bank.notifications.domain.usecase

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.NotificationChannelResponse
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.data.error.HttpResponseInvalidDataException
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.mapper.NotificationChannelDomainMapper
import com.bank.notifications.domain.model.NotificationChannelDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class FetchUserNotificationChannel(
    private val service: NotificationHttpService,
    private val mapper: Mapper<NotificationChannelResponse, NotificationChannelDomain> = NotificationChannelDomainMapper()
) : UseCaseParam1<NotificationChannelDomain, String> {
    override suspend fun invoke(input: String): Flow<UseCaseResult<NotificationChannelDomain, HttpException>> {
        return flow {
            val result = service.getUserNotificationChannel(input)
            if (result.isSuccessful) {
                val notificationChannel = result.body()!!
                when (val mapperResult = mapper.map(notificationChannel)) {
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