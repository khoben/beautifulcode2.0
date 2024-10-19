package com.bank.notifications.data.error

import retrofit2.HttpException
import retrofit2.Response

class HttpResponseInvalidDataException(response: Response<*>) : HttpException(response) {
    private val errorMessage = "Received invalid response data"
    override val message: String = errorMessage
    override fun message(): String = errorMessage
}