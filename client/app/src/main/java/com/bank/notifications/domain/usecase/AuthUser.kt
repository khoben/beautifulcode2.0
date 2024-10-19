package com.bank.notifications.domain.usecase

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.data.cloud.UserAuthedResponse
import com.bank.notifications.data.error.HttpResponseInvalidDataException
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.mapper.AuthUserDomainMapper
import com.bank.notifications.domain.model.UserTokenDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class AuthUser(
    private val service: NotificationHttpService,
    private val mapper: Mapper<UserAuthedResponse, UserTokenDomain> = AuthUserDomainMapper()
) : UseCase<UserTokenDomain> {
    override suspend operator fun invoke(): Flow<UseCaseResult<UserTokenDomain, HttpException>> {
        return flow {
            val result = service.authUser()
            if (result.isSuccessful) {
                val user = result.body()!!
                when (val mapResult = mapper.map(user)) {
                    is MapperResult.Success -> emit(UseCaseResult.Success(mapResult.data))
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