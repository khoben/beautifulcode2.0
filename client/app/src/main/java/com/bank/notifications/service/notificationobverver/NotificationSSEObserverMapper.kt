package com.bank.notifications.service.notificationobverver

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult

class NotificationSSEObserverMapper(private val sseDataRegex: Regex = DEFAULT_SSE_DATA_REGEX) :
    Mapper<String, Map<String, String>> {
    override fun map(data: String): MapperResult<Map<String, String>> {
        return MapperResult.Success(
            sseDataRegex.findAll(data).associate { it.groupValues[1] to it.groupValues[2] }
        )
    }

    companion object {
        private val DEFAULT_SSE_DATA_REGEX = Regex("([^='\\s]+)='([^']+)'")
    }
}