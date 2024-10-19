package com.bank.notifications.presentation

import com.bank.notifications.data.cache.SettingsProvider
import com.bank.notifications.testing.MockPreferencesRepository
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MainViewModelTest {

    private val testDispatcher = Dispatchers.Unconfined
    private lateinit var viewModel: MainViewModel
    private lateinit var mockSettingsManager: SettingsProvider

    @Before
    fun setup() {
        mockSettingsManager = SettingsProvider.Preferences(
            preferencesRepository = MockPreferencesRepository(),
            apiEndpointBaseKey = "apiEndpointBaseKey",
            apiTokenKey = "apiTokenKey",
            defaultApiEndpointBase = "default_endpoint",
            defaultApiTokenValue = "default_token"
        )
        viewModel = MainViewModel(mockSettingsManager, testDispatcher, testDispatcher)
    }

    @Test
    fun `test loading api endpoint`() {
        val expected = "default_endpoint"
        var loaded: String? = null
        viewModel.loadApiEndpoint { loaded = it }
        assertTrue(expected == loaded)
    }

    @Test
    fun `test saving api endpoint`() {
        var loaded: String? = null
        viewModel.loadApiEndpoint { loaded = it }
        assertTrue("test_api_endpoint" != loaded)

        val apiEndpoint = "test_api_endpoint"
        viewModel.storeApiEndpoint(apiEndpoint)
        assertTrue(mockSettingsManager.apiEndpointBase == apiEndpoint)
    }
}


