package com.bank.notifications.presentation.settings.recyclerview

import com.bank.notifications.common.platform.PlatformString
import org.junit.Assert.assertEquals
import org.junit.Test

class MapChannelIdToNameTest {

    private val transactionResourceId = 1
    private val promotionResourceId = 2
    private val chatResourceId = 3
    private val supportResourceId = 4

    private val mapChannelIdToName = MapChannelIdToName.ResourceOrDefault(
        map = mapOf(
            "transactions" to transactionResourceId,
            "promotions" to promotionResourceId,
            "chat" to chatResourceId,
            "support" to supportResourceId
        )
    )

    @Test
    fun `test map channel id with existent key, should return corresponding resource string`() {
        assertEquals(
            PlatformString.Resource(transactionResourceId),
            mapChannelIdToName.map("transactions")
        )
        assertEquals(
            PlatformString.Resource(promotionResourceId),
            mapChannelIdToName.map("promotions")
        )
        assertEquals(PlatformString.Resource(chatResourceId), mapChannelIdToName.map("chat"))
        assertEquals(PlatformString.Resource(supportResourceId), mapChannelIdToName.map("support"))
    }

    @Test
    fun `test map channel id with non-existent key, should return plain string in title case`() {
        assertEquals(
            PlatformString.Plain("Nonexistentchannel"),
            mapChannelIdToName.map("nonexistentchannel")
        )
    }
}
