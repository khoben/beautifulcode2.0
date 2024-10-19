package com.bank.notifications.common.mapper

sealed class MapperResult<T> {
    data class Success<T>(val data: T) : MapperResult<T>()
    class InvalidDataError<T> : MapperResult<T>()
}

fun <T> MapperResult<T>.getOrThrow(): T = when (this) {
    is MapperResult.Success -> data
    else -> throw IllegalStateException("Invalid data error")
}