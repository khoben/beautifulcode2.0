package com.bank.notifications.domain.usecase

import com.bank.notifications.data.cloud.NotificationChannelResponse
import com.bank.notifications.data.cloud.NotificationChannelsResponse
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.model.NotificationChannelDomain
import com.bank.notifications.domain.model.NotificationChannelsDomain
import com.bank.notifications.sharedtest.mock.okhttp.MockResponse
import com.bank.notifications.testing.BaseHttpServiceTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mock.get
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchUserNotificationChannelsTest : BaseHttpServiceTest() {

    @Test
    fun `should return notification channels when response is successful`() {
        val serviceThatReturnsSuccessfulData =
            mockService<NotificationHttpService>(
                baseUrl = "https://notifications.local", responses = listOf(
                    MockResponse(
                        method = get,
                        path = "/api/v1/notifications/channels",
                        status = 200,
                        response = NotificationChannelsResponse(
                            notificationChannels = listOf(
                                NotificationChannelResponse(
                                    channelId = "transactions",
                                    smsEnabled = true,
                                    emailEnabled = true,
                                    pushEnabled = true,
                                )
                            )
                        ).toResponseString()
                    )
                )
            )

        val response = runBlocking {
            FetchUserNotificationChannels(serviceThatReturnsSuccessfulData)().first()
        }

        val expectedResultData = NotificationChannelsDomain(
            listOf(
                NotificationChannelDomain(
                    channelId = "transactions",
                    smsEnabled = true,
                    emailEnabled = true,
                    pushEnabled = true,
                )
            )
        )

        assertTrue(response is UseCaseResult.Success)
        assertEquals(expectedResultData, (response as UseCaseResult.Success).data)
    }

    @Test
    fun `should return error when response is malformed`() {
        val serviceThatReturnsInvalidResponseData =
            mockService<NotificationHttpService>(
                baseUrl = "https://notifications.local", responses = listOf(
                    MockResponse(
                        method = get,
                        path = "/api/v1/notifications/channels",
                        status = 200,
                        response = mapOf("lol" to "kek").toResponseString()
                    )
                )
            )

        val response = runBlocking {
            FetchUserNotificationChannels(serviceThatReturnsInvalidResponseData)().first()
        }

        assertTrue(response is UseCaseResult.Error)
    }

    @Test
    fun `should return error when response is unsuccessful with error status codes`() {
        for (unsuccessfulStatusCode in unsuccessfulStatusCodes) {
            val serviceThatReturnsUnsuccessfulStatus =
                mockService<NotificationHttpService>(
                    baseUrl = "https://notifications.local", responses = listOf(
                        MockResponse(
                            method = get,
                            path = "/api/v1/notifications/channels",
                            status = unsuccessfulStatusCode,
                            response = mapOf("lol" to "kek").toResponseString()
                        )
                    )
                )

            val response = runBlocking {
                FetchUserNotificationChannels(serviceThatReturnsUnsuccessfulStatus)().first()
            }

            assertTrue(response is UseCaseResult.Error)
        }
    }
}