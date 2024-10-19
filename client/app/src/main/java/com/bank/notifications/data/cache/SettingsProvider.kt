package com.bank.notifications.data.cache

interface SettingsProvider {

    var apiEndpointBase: String
    var apiTokenValue: String

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)

    interface Listener {
        fun onApiTokenChanged(value: String)
        fun onApiEndpointChanged(value: String)
    }

    class Preferences(
        private val preferencesRepository: PreferencesRepository,
        private val apiEndpointBaseKey: String = API_ENDPOINT_BASE_KEY,
        private val apiTokenKey: String = API_TOKEN_KEY,
        private val defaultApiEndpointBase: String = DEFAULT_ENDPOINT_BASE,
        private val defaultApiTokenValue: String = DEFAULT_ENDPOINT_TOKEN,
    ) : SettingsProvider {

        private val listeners = mutableSetOf<Listener>()

        private var cachedApiEndpointBase: String? = null
        private var cachedApiTokenValue: String? = null

        override var apiEndpointBase: String
            get() {
                val value = cachedApiEndpointBase ?: preferencesRepository.get(
                    apiEndpointBaseKey,
                    defaultApiEndpointBase
                ).also { cachedApiEndpointBase = it }
                return value
            }
            set(value) {
                if (value == cachedApiEndpointBase) return
                cachedApiEndpointBase = value
                preferencesRepository.save(apiEndpointBaseKey, value)
                for (listener in listeners) {
                    listener.onApiEndpointChanged(value)
                }
            }

        override var apiTokenValue: String
            get() {
                val value = cachedApiTokenValue ?: preferencesRepository.get(
                    apiTokenKey,
                    defaultApiTokenValue
                ).also { cachedApiTokenValue = it }
                return value
            }
            set(value) {
                if (value == cachedApiTokenValue) return
                cachedApiTokenValue = value
                preferencesRepository.save(apiTokenKey, value)
                for (listener in listeners) {
                    listener.onApiTokenChanged(value)
                }
            }

        override fun addListener(listener: Listener) {
            listeners.add(listener)
        }

        override fun removeListener(listener: Listener) {
            listeners.remove(listener)
        }

        companion object {
            private const val API_ENDPOINT_BASE_KEY = "NotificationPrefs:API_ENDPOINT_BASE_KEY"
            private const val API_TOKEN_KEY = "NotificationPrefs:API_TOKEN_KEY"

            private const val DEFAULT_ENDPOINT_BASE = "https://notifications.local"
            private const val DEFAULT_ENDPOINT_TOKEN = ""
        }

    }
}