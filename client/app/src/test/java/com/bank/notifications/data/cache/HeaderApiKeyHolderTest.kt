package com.bank.notifications.data.cache

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HeaderApiKeyHolderTest {

    private lateinit var mockSettingsProvider: SettingsProvider

    @Before
    fun setUp() {
        mockSettingsProvider = object : SettingsProvider {
            override var apiEndpointBase: String = "data"
            override var apiTokenValue: String = "token"
            override fun addListener(listener: SettingsProvider.Listener) = Unit
            override fun removeListener(listener: SettingsProvider.Listener) = Unit
        }
    }

    @Test
    fun `test HeaderApiKeyHolder_X_API_TOKEN header name`() {
        val headerApiKeyHolder =
            HeaderApiKeyHolder.X_API_TOKEN(settingsProvider = mockSettingsProvider)
        assertEquals("X-API-TOKEN", headerApiKeyHolder.header)
    }

    @Test
    fun `test HeaderApiKeyHolder_X_API_TOKEN token value`() {
        val headerApiKeyHolder =
            HeaderApiKeyHolder.X_API_TOKEN(settingsProvider = mockSettingsProvider)
        assertEquals("token", headerApiKeyHolder.value)
    }

    @Test
    fun `test HeaderApiKeyHolder_X_API_TOKEN with custom header`() {
        val headerApiKeyHolder =
            HeaderApiKeyHolder.X_API_TOKEN("custom_header", mockSettingsProvider)

        assertEquals("custom_header", headerApiKeyHolder.header)
        assertEquals("token", headerApiKeyHolder.value)
    }

    @Test
    fun `test HeaderApiKeyHolder_X_API_TOKEN should update token value`() {
        val headerApiKeyHolder =
            HeaderApiKeyHolder.X_API_TOKEN(settingsProvider = mockSettingsProvider)
        assertEquals("token", headerApiKeyHolder.value)
        mockSettingsProvider.apiTokenValue = "new_token"
        assertEquals("new_token", headerApiKeyHolder.value)
    }
}

