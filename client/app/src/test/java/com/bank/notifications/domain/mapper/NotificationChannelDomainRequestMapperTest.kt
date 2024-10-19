package com.bank.notifications.domain.mapper

import com.bank.notifications.common.mapper.MapperResult
import com.bank.notifications.data.cloud.NotificationChannelRequest
import com.bank.notifications.domain.model.NotificationChannelDomainRequest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationChannelDomainRequestMapperTest {
    private lateinit var notificationChannelDomainRequestMapper: NotificationChannelDomainRequestMapper

    @Before
    fun setUp() {
        notificationChannelDomainRequestMapper = NotificationChannelDomainRequestMapper()
    }

    @Test
    fun `test map with valid data`() {
        val mockRequest = NotificationChannelDomainRequest(
            channelId = "test_channel_id",
            smsEnabled = true,
            emailEnabled = true,
            pushEnabled = true,
        )
        val result = notificationChannelDomainRequestMapper.map(mockRequest)
        val expected = NotificationChannelRequest(
            smsEnabled = true,
            emailEnabled = true,
            pushEnabled = true,
        )
        assertTrue(result is MapperResult.Success && result.data == expected)
    }
}