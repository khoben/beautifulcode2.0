package com.bank.notifications.testing

import com.bank.notifications.sharedtest.mock.okhttp.MockOkHttpClient
import com.bank.notifications.sharedtest.mock.okhttp.MockResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

open class BaseHttpServiceTest {

    protected val unsuccessfulStatusCodes = listOf(400, 401, 403, 404, 500, 503)

    protected val ktxJson = Json { ignoreUnknownKeys = true }

    protected inline fun <reified T> mockService(
        baseUrl: String,
        responses: List<MockResponse>,
        requestTimeout: Long? = null,
        responseTimeout: Long? = null
    ): T {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(ktxJson.asConverterFactory("application/json".toMediaType()))
            .client(MockOkHttpClient(baseUrl, responses, requestTimeout, responseTimeout)).build()
            .create(T::class.java)
    }

    protected inline fun <reified T> T.toResponseString(): String {
        return ktxJson.encodeToString(this)
    }
}