package com.bank.notifications.data.cache

import android.content.Context
import android.content.SharedPreferences

interface PreferencesRepository {
    fun get(key: String, default: String): String
    fun get(key: String, default: Int): Int
    fun get(key: String, default: Boolean): Boolean
    fun save(key: String, value: String)
    fun save(key: String, value: Int)
    fun save(key: String, value: Boolean)
    fun remove(key: String)

    class SharedPreferencesRepository(context: Context, name: String) : PreferencesRepository {

        private val read: SharedPreferences =
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
        private val write: SharedPreferences.Editor = read.edit()

        override fun get(key: String, default: String): String {
            return read.getString(key, default) ?: default
        }

        override fun get(key: String, default: Int): Int {
            return read.getInt(key, default)
        }

        override fun get(key: String, default: Boolean): Boolean {
            return read.getBoolean(key, default)
        }

        override fun save(key: String, value: String) {
            write.putString(key, value).apply()
        }

        override fun save(key: String, value: Int) {
            write.putInt(key, value).apply()
        }

        override fun save(key: String, value: Boolean) {
            write.putBoolean(key, value).apply()
        }

        override fun remove(key: String) {
            write.remove(key).apply()
        }
    }
}