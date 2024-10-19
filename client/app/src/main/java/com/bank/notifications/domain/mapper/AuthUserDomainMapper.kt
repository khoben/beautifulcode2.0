package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.UserAuthedResponse
import com.bank.notifications.domain.model.UserTokenDomain

class AuthUserDomainMapper : Mapper<UserAuthedResponse, UserTokenDomain> {
    override fun map(data: UserAuthedResponse) = when {
        data.token == null -> MapperResult.InvalidDataError()
        else -> MapperResult.Success(UserTokenDomain(data.token))
    }
}