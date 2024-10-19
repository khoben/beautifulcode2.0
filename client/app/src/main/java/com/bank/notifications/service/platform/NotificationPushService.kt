package com.bank.notifications.service.platform

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bank.notifications.R
import com.bank.notifications.common.startup.Startup
import com.bank.notifications.data.cache.SettingsProvider
import com.bank.notifications.di.DI
import com.bank.notifications.presentation.MainActivity
import com.bank.notifications.service.notificationobverver.NotificationObserver
import java.util.UUID


class NotificationPushService : Service() {

    private lateinit var notificationSettings: SettingsProvider
    private lateinit var notificationObserver: NotificationObserver

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = restartNotificationObserver()
    }
    private val apiParametersChangedCallback = object : SettingsProvider.Listener {
        override fun onApiTokenChanged(value: String) = restartNotificationObserver()
        override fun onApiEndpointChanged(value: String) =
            restartNotificationObserver(newApiEndpoint = value)
    }
    private val notificationObserverCallback = object : NotificationObserver.Listener {
        override fun onMessage(message: Map<String, String>) {
            val title = message["title"]
            val text = message["text"]
            if (title.isNullOrEmpty() || text.isNullOrEmpty()) return
            sendNotification(title, text)
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel(
            SERVICE_CHANNEL_ID,
            SERVICE_CHANNEL_NAME,
            SERVICE_CHANNEL_DESCRIPTION,
            SERVICE_CHANNEL_IMPORTANCE
        )
        createNotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NOTIFICATION_CHANNEL_DESCRIPTION,
            NOTIFICATION_CHANNEL_IMPORTANCE
        )

        startForegroundWithNotification()

        notificationSettings = DI.notificationSettings
        notificationSettings.addListener(apiParametersChangedCallback)

        notificationObserver = DI.notificationObserver
        notificationObserver.registerListener(notificationObserverCallback)
        notificationObserver.start()

        (getSystemService(ConnectivityManager::class.java) as ConnectivityManager).requestNetwork(
            NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build(), networkCallback
        )
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        (getSystemService(ConnectivityManager::class.java) as ConnectivityManager).unregisterNetworkCallback(
            networkCallback
        )
        notificationSettings.removeListener(apiParametersChangedCallback)
        notificationObserver.unregisterListener(notificationObserverCallback)
        notificationObserver.stop()
    }

    private fun restartNotificationObserver(newApiEndpoint: String? = null) {
        newApiEndpoint?.let { notificationObserver.changeBaseUrl(newApiEndpoint) }
        notificationObserver.stop()
        notificationObserver.start()
    }

    private fun startForegroundWithNotification() {
        startForeground(
            SERVICE_NOTIFICATION_ID, createNotification(
                title = SERVICE_CHANNEL_NAME,
                message = SERVICE_CHANNEL_NAME,
                channelId = SERVICE_CHANNEL_ID,
                priority = NotificationCompat.PRIORITY_DEFAULT
            )
        )
    }

    private fun createNotification(
        title: String,
        message: String,
        channelId: String,
        priority: Int,
    ): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, if (Build.VERSION.SDK_INT >= 34) PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_MUTABLE
        )

        val notification: Notification =
            NotificationCompat.Builder(this, channelId).setContentTitle(title)
                .setContentText(message).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent).setPriority(priority).build()

        return notification
    }

    private fun sendNotification(
        title: String, message: String, notificationId: Int = randomNotificationId()
    ) {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(
            notificationId, createNotification(
                title, message, NOTIFICATION_CHANNEL_ID, NotificationCompat.PRIORITY_HIGH
            )
        )
    }

    private fun createNotificationChannel(
        id: String, name: String, description: String, importance: Int
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        (getSystemService(NotificationManager::class.java)).createNotificationChannel(
            NotificationChannel(id, name, importance).apply {
                this.description = description
            })
    }

    private fun randomNotificationId() = UUID.randomUUID().hashCode()

    companion object : Startup {
        private const val SERVICE_CHANNEL_ID = "BankNotificationsService"
        private const val SERVICE_CHANNEL_NAME = "Bank Notifications Service"
        private const val SERVICE_CHANNEL_DESCRIPTION = "Channel for bank notifications service"
        private const val SERVICE_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT
        private const val SERVICE_NOTIFICATION_ID = 1

        private const val NOTIFICATION_CHANNEL_ID = "BankNotifications"
        private const val NOTIFICATION_CHANNEL_NAME = "Bank Notifications"
        private const val NOTIFICATION_CHANNEL_DESCRIPTION = "Channel for bank notifications"
        private const val NOTIFICATION_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH

        override fun start(context: Context) {
            val intent = Intent(context, NotificationPushService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}