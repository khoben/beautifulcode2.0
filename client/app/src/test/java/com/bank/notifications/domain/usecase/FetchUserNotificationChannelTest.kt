package com.bank.notifications.domain.usecase

import com.bank.notifications.data.cloud.NotificationChannelResponse
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.model.NotificationChannelDomain
import com.bank.notifications.sharedtest.mock.okhttp.MockResponse
import com.bank.notifications.testing.BaseHttpServiceTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mock.get
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchUserNotificationChannelTest : BaseHttpServiceTest() {

    @Test
    fun `should return user notification channel when response is successful`() {
        val serviceThatReturnsSuccessfulData =
            mockService<NotificationHttpService>(
                baseUrl = "https://notifications.local",
                responses = listOf(
                    MockResponse(
                        method = get,
                        path = "/api/v1/notifications/channels/transactions",
                        status = 200,
                        response = NotificationChannelResponse(
                            channelId = "transactions",
                            smsEnabled = true,
                            emailEnabled = true,
                            pushEnabled = true,
                        ).toResponseString()
                    )
                )
            )

        val response = runBlocking {
            FetchUserNotificationChannel(serviceThatReturnsSuccessfulData)("transactions").first()
        }

        val expectedResultData = NotificationChannelDomain(
            channelId = "transactions",
            smsEnabled = true,
            emailEnabled = true,
            pushEnabled = true,
        )

        assertTrue(response is UseCaseResult.Success && response.data == expectedResultData)
    }

    @Test
    fun `should return error when response is malformed`() {
        val serviceThatReturnsInvalidResponseData =
            mockService<NotificationHttpService>(
                baseUrl = "https://notifications.local",
                responses = listOf(
                    MockResponse(
                        method = get,
                        path = "/api/v1/notifications/channels/transactions",
                        status = 200,
                        response = emptyMap<String, String>().toResponseString()
                    )
                )
            )

        val response = runBlocking {
            FetchUserNotificationChannel(serviceThatReturnsInvalidResponseData)("transactions").first()
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
                            method = get,
                            path = "/api/v1/notifications/channels/transactions",
                            status = unsuccessfulStatusCode,
                            response = NotificationChannelResponse(
                                channelId = "transactions",
                                smsEnabled = true,
                                emailEnabled = true,
                                pushEnabled = true,
                            ).toResponseString()
                        )
                    )
                )

            val response = runBlocking {
                FetchUserNotificationChannel(serviceThatReturnsUnsuccessfulStatus)("transactions").first()
            }

            assertTrue(response is UseCaseResult.Error)
        }
    }
}