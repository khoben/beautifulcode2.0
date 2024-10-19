package com.bank.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bank.notifications.data.cache.SettingsProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val settingsManager: SettingsProvider,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    fun loadApiEndpoint(onLoaded: (String) -> Unit) {
        viewModelScope.launch(mainDispatcher) {
            val apiEndpointBase = withContext(ioDispatcher) { settingsManager.apiEndpointBase }
            onLoaded(apiEndpointBase)
        }
    }

    fun storeApiEndpoint(apiEndpoint: String) {
        viewModelScope.launch(ioDispatcher) { settingsManager.apiEndpointBase = apiEndpoint }
    }

    class Factory(
        private val settingsManager: SettingsProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(settingsManager) as T
        }
    }
}