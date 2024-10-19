package com.bank.notifications.data.cache

interface HeaderApiKeyHolder {
    val header: String
    var value: String

    class X_API_TOKEN(
        override val header: String = DEFAULT_HEADER,
        private val settingsProvider: SettingsProvider
    ) : HeaderApiKeyHolder {

        override var value: String
            get() = settingsProvider.apiTokenValue
            set(value) {
                settingsProvider.apiTokenValue = value
            }

        companion object {
            private const val DEFAULT_HEADER = "X-API-TOKEN"
        }
    }
}