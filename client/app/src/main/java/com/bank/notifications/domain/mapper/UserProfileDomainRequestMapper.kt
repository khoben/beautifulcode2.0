package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.UserProfileRequest
import com.bank.notifications.domain.model.UserProfileDomainRequest

class UserProfileDomainRequestMapper : Mapper<UserProfileDomainRequest, UserProfileRequest> {
    override fun map(data: UserProfileDomainRequest): MapperResult<UserProfileRequest> {
        return MapperResult.Success(
            UserProfileRequest(
                email = data.email,
                phone = data.phone
            )
        )
    }
}