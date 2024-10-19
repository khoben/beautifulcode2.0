package com.bank.notifications.testing.local

import com.bank.notifications.data.cache.PreferencesRepository

class MockPreferencesRepository : PreferencesRepository {

    private var memoryStorageString = mutableMapOf<String, String>()
    private var memoryStorageInt = mutableMapOf<String, Int>()
    private var memoryStorageBoolean = mutableMapOf<String, Boolean>()

    override fun get(key: String, default: String): String {
        return memoryStorageString[key] ?: default
    }

    override fun get(key: String, default: Int): Int {
        return memoryStorageInt[key] ?: default
    }

    override fun get(key: String, default: Boolean): Boolean {
        return memoryStorageBoolean[key] ?: default
    }

    override fun save(key: String, value: String) {
        memoryStorageString[key] = value
    }

    override fun save(key: String, value: Int) {
        memoryStorageInt[key] = value
    }

    override fun save(key: String, value: Boolean) {
        memoryStorageBoolean[key] = value
    }

    override fun remove(key: String) {
        if (key in memoryStorageString) {
            memoryStorageString.remove(key)
            return
        }
        if (key in memoryStorageInt) {
            memoryStorageInt.remove(key)
            return
        }
        if (key in memoryStorageBoolean) {
            memoryStorageBoolean.remove(key)
            return
        }
    }

}