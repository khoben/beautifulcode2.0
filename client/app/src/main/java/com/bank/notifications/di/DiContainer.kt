package com.bank.notifications.di

import com.bank.notifications.common.startup.Startup
import com.bank.notifications.data.cache.HeaderApiKeyHolder
import com.bank.notifications.data.cache.SettingsProvider
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.domain.ValidateUserData
import com.bank.notifications.service.notificationobverver.NotificationObserver

interface DiContainer {
    val startup: Startup
    val notificationSettings: SettingsProvider
    val apiKeyHolder: HeaderApiKeyHolder
    val notificationHttpService: NotificationHttpService
    val notificationObserver: NotificationObserver
    val validateUserData: ValidateUserData
}