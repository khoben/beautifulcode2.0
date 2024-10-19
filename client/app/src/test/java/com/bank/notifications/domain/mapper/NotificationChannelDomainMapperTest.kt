package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.NotificationChannelResponse
import com.bank.notifications.domain.model.NotificationChannelDomain
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationChannelDomainMapperTest {
    private lateinit var notificationChannelDomainMapper: NotificationChannelDomainMapper

    @Before
    fun setUp() {
        notificationChannelDomainMapper = NotificationChannelDomainMapper()
    }

    @Test
    fun `test map with valid data`() {
        val mockResponse = NotificationChannelResponse(
            channelId = "test_channel_id",
            smsEnabled = true,
            emailEnabled = true,
            pushEnabled = true,
        )
        val result = notificationChannelDomainMapper.map(mockResponse)
        val expected = NotificationChannelDomain(
            channelId = "test_channel_id",
            smsEnabled = true,
            emailEnabled = true,
            pushEnabled = true,
        )
        assertTrue(result is MapperResult.Success && result.data == expected)
    }

    @Test
    fun `test map with invalid data`() {
        var mockResponse = NotificationChannelResponse()
        var result = notificationChannelDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

        mockResponse = NotificationChannelResponse(channelId = "channel_id")
        result = notificationChannelDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

        mockResponse =
            NotificationChannelResponse(channelId = "channel_id", smsEnabled = true)
        result = notificationChannelDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

        mockResponse = NotificationChannelResponse(
            channelId = "channel_id",
            smsEnabled = true,
            emailEnabled = true
        )
        result = notificationChannelDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)

        mockResponse = NotificationChannelResponse(
            smsEnabled = true,
            emailEnabled = true,
            pushEnabled = true
        )
        result = notificationChannelDomainMapper.map(mockResponse)
        assertTrue(result is MapperResult.InvalidDataError)
    }
}