package com.bank.notifications.sharedtest.mock.okhttp

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mock.MockInterceptor
import okhttp3.mock.eq
import okhttp3.mock.rule
import okhttp3.mock.url
import java.util.concurrent.TimeUnit

fun MockOkHttpClient(
    baseUrl: String,
    mockResponses: List<MockResponse>,
    requestTimeout: Long? = null,
    responseTimeout: Long? = null
) = OkHttpClient().newBuilder().apply {
    if (requestTimeout != null) {
        connectTimeout(requestTimeout, TimeUnit.MILLISECONDS)
        readTimeout(requestTimeout, TimeUnit.MILLISECONDS)
        writeTimeout(requestTimeout, TimeUnit.MILLISECONDS)
    }
}.addInterceptor(MockInterceptor().apply {
    for (mockedResponse in mockResponses) {
        rule(
            mockedResponse.method,
            url eq baseUrl.plus(mockedResponse.path),
            delay = responseTimeout
        ) {
            respond(
                mockedResponse.status,
                mockedResponse.response.toResponseBody(mockedResponse.mediaType.toMediaTypeOrNull()),
            )
        }
    }
}).build()

