package com.bank.notifications.service

import com.bank.notifications.service.notificationobverver.NotificationObserver
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.mock.MockInterceptor
import okhttp3.mock.eq
import okhttp3.mock.get
import okhttp3.mock.rule
import okhttp3.mock.url
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class NotificationSSEObserverTest {

    private val DEFAULT_TIMEOUT_SEC = 3L
    private val DEFAULT_BASE_URL = "https://notifications.local"
    private val DEFAULT_PATH = "/api/v1/notifications/events"

    private fun createStreamingInterceptor(
        baseUrl: String, path: String, data: List<String>, delayMs: Long = 50L
    ): MockInterceptor {
        return MockInterceptor().apply {
            rule(get, url eq baseUrl.plus(path)) {

                val input = PipedInputStream(4096)
                val output = PipedOutputStream(input)

                thread(start = true) {
                    for (item in data) {
                        output.write(item.toByteArray())
                        Thread.sleep(delayMs)
                    }
                    output.close()
                }

                respond(input, "text/event-stream".toMediaTypeOrNull()!!)
            }
        }
    }

    @Test
    fun `test sse single success response`() {
        val mockInterceptor = createStreamingInterceptor(
            DEFAULT_BASE_URL, DEFAULT_PATH, listOf(
                """
              |data: title='deposit' message='+228₽'
              |
              |
              """.trimMargin(),
            )
        )

        val observer = NotificationObserver.SSE(
            baseUrl = DEFAULT_BASE_URL, path = DEFAULT_PATH, interceptor = mockInterceptor
        )
        var actualMessage: Map<String, String>? = null
        val lock = CountDownLatch(1)
        observer.registerListener(object : NotificationObserver.Listener {
            override fun onMessage(message: Map<String, String>) {
                actualMessage = message
                lock.countDown()
            }
        })
        observer.start()
        lock.await(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS)

        assertEquals(mapOf("title" to "deposit", "message" to "+228₽"), actualMessage)
    }

    @Test
    fun `test sse many success response`() {
        val mockInterceptor = createStreamingInterceptor(
            DEFAULT_BASE_URL, DEFAULT_PATH, listOf(
                """
              |data: title='deposit' message='+228₽'
              |
              |
              """.trimMargin(),
                """
              |data: title='withdrawal' message='-228₽'
              |
              |
              """.trimMargin(),
            )
        )

        val observer = NotificationObserver.SSE(
            baseUrl = DEFAULT_BASE_URL, path = DEFAULT_PATH, interceptor = mockInterceptor
        )
        val actualMessages = mutableListOf<Map<String, String>>()
        val lock = CountDownLatch(2) // expected 2 messages
        observer.registerListener(object : NotificationObserver.Listener {
            override fun onMessage(message: Map<String, String>) {
                actualMessages.add(message)
                lock.countDown()
            }
        })
        observer.start()
        lock.await(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS)

        assertEquals(
            listOf(
                mapOf("title" to "deposit", "message" to "+228₽"),
                mapOf("title" to "withdrawal", "message" to "-228₽")
            ), actualMessages
        )
    }

    @Test
    fun `test register unregister listener`() {
        val mockInterceptor = createStreamingInterceptor(
            DEFAULT_BASE_URL, DEFAULT_PATH, listOf(
                """
              |data: title='deposit' message='+228₽'
              |
              |
              """.trimMargin(),
                """
              |data: title='withdrawal' message='-228₽'
              |
              |
              """.trimMargin(),
            )
        )

        val observer = NotificationObserver.SSE(
            baseUrl = DEFAULT_BASE_URL, path = DEFAULT_PATH, interceptor = mockInterceptor
        )
        val actualMessages = mutableListOf<Map<String, String>>()
        val actualMessagesSecond = mutableListOf<Map<String, String>>()
        val lock = CountDownLatch(2) // expected 2 messages
        val secondListener = object : NotificationObserver.Listener {
            override fun onMessage(message: Map<String, String>) {
                actualMessagesSecond.add(message)
                lock.countDown()
            }
        }
        val firstListener = object : NotificationObserver.Listener {
            override fun onMessage(message: Map<String, String>) {
                actualMessages.add(message)
                observer.unregisterListener(this)
                observer.registerListener(secondListener)
                lock.countDown()
            }
        }
        observer.registerListener(firstListener)
        observer.start()
        lock.await(DEFAULT_TIMEOUT_SEC, TimeUnit.SECONDS)

        assertTrue(actualMessages.size == 1)
        assertEquals(
            listOf(
                mapOf("title" to "deposit", "message" to "+228₽"),
            ), actualMessages
        )

        assertTrue(actualMessagesSecond.size == 1)
        assertEquals(
            listOf(
                mapOf("title" to "withdrawal", "message" to "-228₽"),
            ), actualMessagesSecond
        )
    }

}