package com.bank.notifications.service.notificationobverver


import com.bank.notifications.common.mapper.MapperResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationSSEObserverMapperTest {

    private lateinit var notificationSSEObserverMapper: NotificationSSEObserverMapper

    @Before
    fun setup() {
        notificationSSEObserverMapper = NotificationSSEObserverMapper()
    }

    @Test
    fun `test map success`() {
        val data = "event='notification' id='123' name='test'"
        val result = notificationSSEObserverMapper.map(data)
        assertTrue(result is MapperResult.Success)

        val mappedResult = (result as MapperResult.Success).data
        assertEquals("notification", mappedResult["event"])
        assertEquals("123", mappedResult["id"])
        assertEquals("test", mappedResult["name"])
    }

    @Test
    fun `test map empty map on invalid data`() {
        for (data in listOf("invalid data", "", "'", "''=''", "a='sd", "c=123", "a = '123'")) {
            val result = notificationSSEObserverMapper.map(data)
            assertTrue(result is MapperResult.Success)
            val mappedResult = (result as MapperResult.Success).data
            assertEquals(emptyMap<String, String>(), mappedResult)
        }
    }
}