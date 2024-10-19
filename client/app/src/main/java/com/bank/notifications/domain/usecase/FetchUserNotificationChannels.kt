package com.bank.notifications.domain.usecase

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.NotificationChannelsResponse
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.data.error.HttpResponseInvalidDataException
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.mapper.NotificationChannelsDomainMapper
import com.bank.notifications.domain.model.NotificationChannelsDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class FetchUserNotificationChannels(
    private val service: NotificationHttpService,
    private val mapper: Mapper<NotificationChannelsResponse, NotificationChannelsDomain> = NotificationChannelsDomainMapper()
) : UseCase<NotificationChannelsDomain> {
    override suspend fun invoke(): Flow<UseCaseResult<NotificationChannelsDomain, HttpException>> {
        return flow {
            val result = service.getUserNotificationChannels()
            if (result.isSuccessful) {
                val notificationChannels = result.body()!!
                when (val data = mapper.map(notificationChannels)) {
                    is MapperResult.Success -> emit(UseCaseResult.Success(data.data))
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