package com.bank.notifications.service.notificationobverver

import com.bank.notifications.common.mapper.Mapper
import com.bank.notifications.common.mapper.MapperResult
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.DefaultRetryDelayStrategy
import com.launchdarkly.eventsource.ErrorStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler
import com.launchdarkly.eventsource.background.BackgroundEventSource
import okhttp3.Interceptor
import timber.log.Timber
import java.net.URI
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface NotificationObserver {
    fun start()
    fun stop()
    fun changeBaseUrl(baseUrl: String)
    fun registerListener(listener: Listener)
    fun unregisterListener(listener: Listener)

    interface Listener {
        fun onMessage(message: Map<String, String>)

        object NOOP : Listener {
            override fun onMessage(message: Map<String, String>) = Unit
        }
    }

    class SSE(
        private var baseUrl: String,
        private val path: String,
        private val interceptor: Interceptor,
        private val mapper: Mapper<String, Map<String, String>> = NotificationSSEObserverMapper()
    ) : NotificationObserver {

        private var listener: Listener = Listener.NOOP
        private var eventSource: BackgroundEventSource? = null
        private var shutdownExecutor: ExecutorService? = null

        override fun start() {
            shutdownExecutor = Executors.newSingleThreadExecutor()
            BackgroundEventSource.Builder(
                object : BackgroundEventHandler {
                    override fun onOpen() = Timber.d("Connection opened")

                    override fun onClosed() = Timber.d("Connection closed")

                    override fun onMessage(event: String, messageEvent: MessageEvent) {
                        Timber.d("New message: $event: $messageEvent")
                        when (val mapResult = mapper.map(messageEvent.data)) {
                            is MapperResult.Success -> listener.onMessage(mapResult.data)
                            is MapperResult.InvalidDataError -> Timber.w("Received invalid data: ${messageEvent.data}")
                        }
                    }

                    override fun onComment(comment: String) = Unit
                    override fun onError(t: Throwable) = Timber.e(t)
                }, EventSource.Builder(
                    ConnectStrategy.http(URI.create(baseUrl.plus(path)))
                        .connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(0, TimeUnit.SECONDS)
                        .clientBuilderActions { clientBuilder ->
                            clientBuilder.addInterceptor(interceptor)
                        }
                ).retryDelay(10, TimeUnit.SECONDS)
                    .retryDelayStrategy(
                        DefaultRetryDelayStrategy.defaultStrategy().maxDelay(10, TimeUnit.SECONDS)
                    )
                    .errorStrategy(ErrorStrategy.alwaysContinue())
            ).build().also { eventSource = it }.start()
        }

        override fun stop() {
            val eventSourceToShutdown = eventSource ?: return
            eventSource = null
            // May be called from MainThread with`NetworkOnMainThreadException`
            // So offload eventSource.close to background thread
            shutdownExecutor?.let { executor ->
                executor.execute { eventSourceToShutdown.close() }
                executor.shutdown()
            }
            shutdownExecutor = null
        }

        override fun changeBaseUrl(baseUrl: String) {
            this.baseUrl = baseUrl
        }

        override fun registerListener(listener: Listener) {
            this.listener = listener
        }

        override fun unregisterListener(listener: Listener) {
            this.listener = Listener.NOOP
        }
    }
}