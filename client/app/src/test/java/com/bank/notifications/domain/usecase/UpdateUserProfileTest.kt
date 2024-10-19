package com.bank.notifications.domain.usecase

import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.data.cloud.UserProfileResponse
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.model.UserProfileDomain
import com.bank.notifications.domain.model.UserProfileDomainRequest
import com.bank.notifications.sharedtest.mock.okhttp.MockResponse
import com.bank.notifications.testing.BaseHttpServiceTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mock.post
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateUserProfileTest : BaseHttpServiceTest() {
    @Test
    fun `should return user profile when update response is successful`() {
        val serviceThatReturnsSuccess = mockService<NotificationHttpService>(
            baseUrl = "https://notifications.local", responses = listOf(
                MockResponse(
                    method = post,
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
            UpdateUserProfile(serviceThatReturnsSuccess)(
                UserProfileDomainRequest(
                    email = "lol@mda.kek", phone = "+123456789"
                )
            ).first()
        }

        val expectedResultData = UserProfileDomain(
            "lol@mda.kek",
            "+123456789",
        )

        assertTrue(response is UseCaseResult.Success && response.data == expectedResultData)
    }

    @Test
    fun `should return error when response is malformed`() {
        val serviceThatReturnsIncorrectData =
            mockService<NotificationHttpService>(
                baseUrl = "https://notifications.local", responses = listOf(
                    MockResponse(
                        method = post,
                        path = "/api/v1/user/profile",
                        status = 200,
                        response = mapOf("lol@mda.kek" to "123456789").toResponseString()
                    )
                )
            )

        val response = runBlocking {
            UpdateUserProfile(serviceThatReturnsIncorrectData)(
                UserProfileDomainRequest(
                    email = "lol@mda.kek", phone = "+123456789"
                )
            ).first()
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
                            method = post,
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
                UpdateUserProfile(serviceThatReturnsUnsuccessfulStatus)(
                    UserProfileDomainRequest(
                        email = "lol@mda.kek", phone = "+123456789"
                    )
                ).first()
            }

            assertTrue(response is UseCaseResult.Error)
        }
    }
}