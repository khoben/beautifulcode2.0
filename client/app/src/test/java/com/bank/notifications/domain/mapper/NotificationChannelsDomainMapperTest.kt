package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.NotificationChannelResponse
import com.bank.notifications.data.cloud.NotificationChannelsResponse
import com.bank.notifications.domain.model.NotificationChannelDomain
import com.bank.notifications.domain.model.NotificationChannelsDomain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationChannelsDomainMapperTest {
    private lateinit var notificationChannelsDomainMapper: NotificationChannelsDomainMapper

    @Before
    fun setUp() {
        notificationChannelsDomainMapper = NotificationChannelsDomainMapper()
    }

    @Test
    fun `test map with valid data`() {
        val mockResponse = NotificationChannelsResponse(
            listOf(
                NotificationChannelResponse(
                    channelId = "test_channel_id",
                    smsEnabled = true,
                    emailEnabled = false,
                    pushEnabled = true,
                )
            )
        )
        val result = notificationChannelsDomainMapper.map(mockResponse)
        val expected = NotificationChannelsDomain(
            listOf(
                NotificationChannelDomain(
                    channelId = "test_channel_id",
                    smsEnabled = true,
                    emailEnabled = false,
                    pushEnabled = true,
                )
            )
        )
        assertTrue(result is MapperResult.Success && result.data == expected)
    }

    @Test
    fun `test map with invalid data`() {
        var mockResponse = NotificationChannelsResponse()
        var result = notificationChannelsDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

        mockResponse = NotificationChannelsResponse(
            listOf(
                NotificationChannelResponse(
                    smsEnabled = true,
                    emailEnabled = false,
                    pushEnabled = true,
                )
            )
        )
        result = notificationChannelsDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

        mockResponse = NotificationChannelsResponse(
            listOf(
                NotificationChannelResponse(
                    emailEnabled = false,
                    pushEnabled = true,
                )
            )
        )
        result = notificationChannelsDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

        mockResponse = NotificationChannelsResponse(
            listOf(
                NotificationChannelResponse(
                    pushEnabled = true,
                )
            )
        )
        result = notificationChannelsDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

        mockResponse = NotificationChannelsResponse(
            listOf(
                NotificationChannelResponse()
            )
        )
        result = notificationChannelsDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

    }
}