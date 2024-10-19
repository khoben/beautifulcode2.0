package com.bank.notifications.domain.usecase

import com.bank.notifications.domain.common.UseCaseResult
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

interface UseCaseParam1<T, P1> {
    suspend operator fun invoke(input: P1): Flow<UseCaseResult<T, HttpException>>
}