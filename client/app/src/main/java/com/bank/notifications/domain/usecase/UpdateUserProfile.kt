package com.bank.notifications.domain.usecase

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.common.mapper.getOrThrow
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.data.cloud.UserProfileRequest
import com.bank.notifications.data.cloud.UserProfileResponse
import com.bank.notifications.data.error.HttpResponseInvalidDataException
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.mapper.UserProfileDomainMapper
import com.bank.notifications.domain.mapper.UserProfileDomainRequestMapper
import com.bank.notifications.domain.model.UserProfileDomain
import com.bank.notifications.domain.model.UserProfileDomainRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class UpdateUserProfile(
    private val service: NotificationHttpService,
    private val inMapper: Mapper<UserProfileDomainRequest, UserProfileRequest> = UserProfileDomainRequestMapper(),
    private val outMapper: Mapper<UserProfileResponse, UserProfileDomain> = UserProfileDomainMapper()
) :
    UseCaseParam1<UserProfileDomain, UserProfileDomainRequest> {
    override suspend fun invoke(input: UserProfileDomainRequest): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
        return flow {
            val result = service.updateUserProfile(inMapper.map(input).getOrThrow())
            if (result.isSuccessful) {
                val userProfile = result.body()!!
                when (val data = outMapper.map(userProfile)) {
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