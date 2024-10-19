package com.bank.notifications.domain.usecase

import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.data.cloud.UserAuthedResponse
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.sharedtest.mock.okhttp.MockResponse
import com.bank.notifications.testing.BaseHttpServiceTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mock.post
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUserTest : BaseHttpServiceTest() {

    @Test
    fun `should return user token when response is successful`() {
        val serviceThatReturnsSuccessfulData =
            mockService<NotificationHttpService>(
                baseUrl = "https://notifications.local",
                responses = listOf(
                    MockResponse(
                        method = post,
                        path = "/api/v1/user/auth",
                        status = 200,
                        response = UserAuthedResponse(token = "token").toResponseString()
                    )
                )
            )

        val response = runBlocking {
            AuthUser(serviceThatReturnsSuccessfulData)().first()
        }

        assertTrue(response is UseCaseResult.Success && response.data.token == "token")
    }

    @Test
    fun `should return error when response is malformed`() {
        val serviceThatReturnsInvalidResponseData =
            mockService<NotificationHttpService>(
                baseUrl = "https://notifications.local",
                responses = listOf(
                    MockResponse(
                        method = post,
                        path = "/api/v1/user/auth",
                        status = 200,
                        response = mapOf("chpoken" to "token").toResponseString()
                    )
                )
            )

        val response = runBlocking {
            AuthUser(serviceThatReturnsInvalidResponseData)().first()
        }

        assertTrue(response is UseCaseResult.Error)
    }

    @Test
    fun `should return error when response is unsuccessful with error status codes`() {
        for (unsuccessfulStatusCode in unsuccessfulStatusCodes) {
            val serviceThatReturnsUnsuccessfulStatus =
                mockService<NotificationHttpService>(
                    baseUrl = "https://notifications.local",
                    responses = listOf(
                        MockResponse(
                            method = post,
                            path = "/api/v1/user/auth",
                            status = unsuccessfulStatusCode,
                            response = UserAuthedResponse(token = "token").toResponseString()
                        )
                    )
                )

            val response = runBlocking {
                AuthUser(serviceThatReturnsUnsuccessfulStatus)().first()
            }

            assertTrue(response is UseCaseResult.Error)
        }
    }
}