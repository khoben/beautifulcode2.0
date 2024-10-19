package com.bank.notifications.domain.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

sealed class UseCaseResult<out T, out U> {
    data class Success<T>(val data: T) : UseCaseResult<T, Nothing>()
    data class Error<U>(val error: U) : UseCaseResult<Nothing, U>()
}

inline fun <T, U> UseCaseResult<T, U>.fold(
    crossinline onSuccess: (T) -> Unit, crossinline onError: (U) -> Unit = {}
) = when (this) {
    is UseCaseResult.Success -> onSuccess(data)
    is UseCaseResult.Error -> onError(error)
}

fun <T1, T2, U : Throwable, R> combine(
    first: Flow<UseCaseResult<T1, U>>,
    other: Flow<UseCaseResult<T2, U>>,
    transform: (T1, T2) -> R
): Flow<UseCaseResult<R, U>> {
    return first.combine(other) { result1, result2 ->
        when (result1) {
            is UseCaseResult.Success -> {
                when (result2) {
                    is UseCaseResult.Success -> {
                        UseCaseResult.Success(transform(result1.data, result2.data))
                    }

                    is UseCaseResult.Error -> {
                        UseCaseResult.Error(result2.error)
                    }
                }
            }

            is UseCaseResult.Error -> {
                UseCaseResult.Error(result1.error)
            }
        }
    }
}

fun <T1, T2, U : Throwable, R> chain(
    first: Flow<UseCaseResult<T1, U>>,
    other: Flow<UseCaseResult<T2, U>>,
    transform: (T1, T2) -> R
): Flow<UseCaseResult<R, U>> {
    return flow {
        first.collectSuccessOrEmitError(upstreamCollector = this) { result1 ->
            other.collectSuccessOrEmitError(upstreamCollector = this) { result2 ->
                emit(UseCaseResult.Success(transform(result1, result2)))
            }
        }
    }
}

suspend fun <T, R, U : Throwable> Flow<UseCaseResult<T, U>>.collectSuccessOrEmitError(
    upstreamCollector: FlowCollector<UseCaseResult<R, U>>, successCollector: FlowCollector<T>
) = collect {
    when (it) {
        is UseCaseResult.Success -> successCollector.emit(it.data)
        is UseCaseResult.Error -> upstreamCollector.emit(UseCaseResult.Error(it.error))
    }
}