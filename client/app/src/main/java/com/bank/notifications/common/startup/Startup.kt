package com.bank.notifications.common.startup

import android.content.Context

interface Startup {
    fun start(context: Context)

    class Empty : Startup {
        override fun start(context: Context) = Unit
    }

    class Group(private val group: List<Startup>) : Startup {
        override fun start(context: Context) {
            for (item in group) {
                item.start(context)
            }
        }
    }
}