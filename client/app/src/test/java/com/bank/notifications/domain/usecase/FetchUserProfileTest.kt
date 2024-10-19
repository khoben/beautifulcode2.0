package com.bank.notifications.domain.usecase

import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.data.cloud.UserProfileResponse
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.model.UserProfileDomain
import com.bank.notifications.sharedtest.mock.okhttp.MockResponse
import com.bank.notifications.testing.BaseHttpServiceTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mock.get
import org.junit.Assert.assertTrue
import org.junit.Test

class FetchUserProfileTest : BaseHttpServiceTest() {
    @Test
    fun `should return user profile when response is successful`() {
        val serviceThatReturnsSuccessfulData =
            mockService<NotificationHttpService>(
                baseUrl = "https://notifications.local",
                responses = listOf(
                    MockResponse(
                        method = get,
                        path = "/api/v1/user/profile",
                        status = 200,
                        response = UserProfileResponse(
                            "lol@mda.kek",
                            "+123456789",
                        ).toResponseString()
                    )
                )
            )

        val response = runBlocking {
            FetchUserProfile(serviceThatReturnsSuccessfulData)().first()
        }

        val expectedResultData = UserProfileDomain(
            "lol@mda.kek",
            "+123456789",
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
                        path = "/api/v1/user/profile",
                        status = 200,
                        response = mapOf("lol@mda.kek" to "123456789").toResponseString()
                    )
                )
            )

        val response = runBlocking {
            FetchUserProfile(serviceThatReturnsInvalidResponseData)().first()
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
                            path = "/api/v1/user/profile",
                            status = unsuccessfulStatusCode,
                            response = UserProfileResponse(
                                "lol@mda.kek",
                                "+123456789",
                            ).toResponseString()
                        )
                    )
                )

            val response = runBlocking {
                FetchUserProfile(serviceThatReturnsUnsuccessfulStatus)().first()
            }

            assertTrue(response is UseCaseResult.Error)
        }
    }
}