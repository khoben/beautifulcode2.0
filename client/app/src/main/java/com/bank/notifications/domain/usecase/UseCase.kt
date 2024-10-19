package com.bank.notifications.domain.usecase

import com.bank.notifications.domain.common.UseCaseResult
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

interface UseCase<T> {
    suspend operator fun invoke(): Flow<UseCaseResult<T, HttpException>>
}