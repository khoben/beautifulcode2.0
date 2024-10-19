package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.UserProfileResponse
import com.bank.notifications.domain.model.UserProfileDomain

class UserProfileDomainMapper : Mapper<UserProfileResponse, UserProfileDomain> {
    override fun map(data: UserProfileResponse): MapperResult<UserProfileDomain> {
        return when {
            data.email == null || data.phone == null -> MapperResult.InvalidDataError()
            else -> MapperResult.Success(UserProfileDomain(data.email, data.phone))
        }
    }
}