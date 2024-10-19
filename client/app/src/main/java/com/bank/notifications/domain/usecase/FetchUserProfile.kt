package com.bank.notifications.domain.usecase

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.data.cloud.UserProfileResponse
import com.bank.notifications.data.error.HttpResponseInvalidDataException
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.mapper.UserProfileDomainMapper
import com.bank.notifications.domain.model.UserProfileDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class FetchUserProfile(
    private val service: NotificationHttpService,
    private val mapper: Mapper<UserProfileResponse, UserProfileDomain> = UserProfileDomainMapper()
) : UseCase<UserProfileDomain> {
    override suspend fun invoke(): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
        return flow {
            val result = service.getUserProfile()
            if (result.isSuccessful) {
                val userProfile = result.body()!!
                when (val data = mapper.map(userProfile)) {
                    is MapperResult.Success -> emit(UseCaseResult.Success(data.data))
                    is MapperResult.InvalidDataError -> emit(
                        UseCaseResult.Error(
                            HttpResponseInvalidDataException(result)
                        )
                    )
                }
            } else {
                emit(UseCaseResult.Error(HttpException(result)))
            }
        }
    }
}