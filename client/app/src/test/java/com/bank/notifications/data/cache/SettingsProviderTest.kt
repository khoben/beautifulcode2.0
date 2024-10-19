package com.bank.notifications.data.cache

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsProviderTest {

    private val apiEndpointBaseKey = "apiEndpointBaseKey"
    private val apiTokenKey = "apiTokenKey"

    private lateinit var mockPreferencesRepository: PreferencesRepository
    private lateinit var settingsProvider: SettingsProvider

    @Before
    fun setUp() {
        mockPreferencesRepository = object : PreferencesRepository {
            private var mockApiEndpointBaseValue: String? = null
            private var mockApiTokenValue: String? = null
            override fun get(key: String, default: String): String {
                if (key == apiEndpointBaseKey) {
                    return mockApiEndpointBaseValue ?: default
                }
                if (key == apiTokenKey) {
                    return mockApiTokenValue ?: default
                }
                throw IllegalArgumentException("Unknown key $key")
            }

            override fun get(key: String, default: Int): Int {
                TODO("Not yet implemented")
            }

            override fun get(key: String, default: Boolean): Boolean {
                TODO("Not yet implemented")
            }

            override fun save(key: String, value: String) {
                if (key == apiEndpointBaseKey) {
                    mockApiEndpointBaseValue = value
                    return
                }
                if (key == apiTokenKey) {
                    mockApiTokenValue = value
                    return
                }
                throw IllegalArgumentException("Unknown key $key")
            }

            override fun save(key: String, value: Int) {
                TODO("Not yet implemented")
            }

            override fun save(key: String, value: Boolean) {
                TODO("Not yet implemented")
            }

            override fun remove(key: String) {
                TODO("Not yet implemented")
            }
        }

        settingsProvider = SettingsProvider.Preferences(
            preferencesRepository = mockPreferencesRepository,
            apiEndpointBaseKey = apiEndpointBaseKey,
            apiTokenKey = apiTokenKey,
            defaultApiEndpointBase = "default_endpoint",
            defaultApiTokenValue = "default_token"
        )
    }

    @Test
    fun `test SettingsProvider with defaults`() {
        assertEquals("default_token", settingsProvider.apiTokenValue)
        assertEquals("default_endpoint", settingsProvider.apiEndpointBase)
    }

    @Test
    fun `test SettingsProvider change values`() {
        settingsProvider.apiEndpointBase = "new_endpoint"
        assertTrue(mockPreferencesRepository.get(apiEndpointBaseKey, "") == "new_endpoint")
        assertTrue(settingsProvider.apiEndpointBase == "new_endpoint")

        settingsProvider.apiTokenValue = "new_token"
        assertTrue(mockPreferencesRepository.get(apiTokenKey, "") == "new_token")
        assertTrue(settingsProvider.apiTokenValue == "new_token")
    }

    @Test
    fun `test SettingsProvider listener registration and unregistration`() {
        var newEndpoint: String? = null
        var newToken: String? = null
        val listener = object : SettingsProvider.Listener {
            override fun onApiTokenChanged(value: String) {
                newToken = value
            }

            override fun onApiEndpointChanged(value: String) {
                newEndpoint = value
            }
        }

        assertTrue(settingsProvider.apiTokenValue != newToken)
        assertTrue(settingsProvider.apiEndpointBase != newEndpoint)

        settingsProvider.addListener(listener)
        settingsProvider.apiEndpointBase = "new_endpoint"
        assertTrue(settingsProvider.apiEndpointBase == newEndpoint)
        settingsProvider.apiTokenValue = "new_token"
        assertTrue(settingsProvider.apiTokenValue == newToken)

        settingsProvider.removeListener(listener)
        settingsProvider.apiEndpointBase = "another_new_endpoint"
        assertTrue(settingsProvider.apiEndpointBase != newEndpoint)
        settingsProvider.apiTokenValue = "another_new_token"
        assertTrue(settingsProvider.apiTokenValue != newToken)
    }
}

