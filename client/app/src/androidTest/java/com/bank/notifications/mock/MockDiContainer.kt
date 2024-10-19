package com.bank.notifications.mock

import com.bank.notifications.common.startup.Startup
import com.bank.notifications.data.cache.HeaderApiKeyHolder
import com.bank.notifications.data.cache.SettingsProvider
import com.bank.notifications.data.cloud.NotificationChannelResponse
import com.bank.notifications.data.cloud.NotificationChannelsResponse
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.data.cloud.UserAuthedResponse
import com.bank.notifications.data.cloud.UserProfileResponse
import com.bank.notifications.di.DiContainer
import com.bank.notifications.domain.ValidateUserData
import com.bank.notifications.service.notificationobverver.NotificationObserver
import com.bank.notifications.testing.local.MockPreferencesRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mock.Behavior
import okhttp3.mock.MockInterceptor
import okhttp3.mock.eq
import okhttp3.mock.get
import okhttp3.mock.post
import okhttp3.mock.rule
import okhttp3.mock.url
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MockDiContainer(var config: MockDiConfig = MockDiConfig()) : DiContainer {

    private val BASE_URL = "https://notifications.local"

    private val ktxJson = Json { ignoreUnknownKeys = true }
    private inline fun <reified T> T.toJsonResponse() =
        ktxJson.encodeToString(this).toResponseBody("application/json".toMediaTypeOrNull())

    private fun mockInterceptor() = MockInterceptor().behavior(Behavior.RELAYED).apply {
        rule(
            post, url eq BASE_URL.plus("/api/v1/user/auth")
        ) {
            if (config.authSuccess) {
                respond(
                    200,
                    UserAuthedResponse(token = "token").toJsonResponse(),
                )
            } else {
                respond(
                    403,
                    mapOf("error" to "Unauthorized").toJsonResponse(),
                )
            }
        }
        rule(
            post, url eq BASE_URL.plus("/api/v1/user/profile")
        ) {
            if (config.postUserProfileSuccess) {
                respond(
                    200,
                    UserProfileResponse(
                        config.userEmail,
                        config.userPhone,
                    ).toJsonResponse(),
                )
            } else {
                respond(
                    403,
                    mapOf("error" to "Unauthorized").toJsonResponse(),
                )
            }
        }
        rule(
            get, url eq BASE_URL.plus("/api/v1/user/profile")
        ) {
            if (config.getUserProfileSuccess) {
                respond(
                    200,
                    UserProfileResponse(
                        config.userEmail,
                        config.userPhone,
                    ).toJsonResponse(),
                )
            } else {
                respond(
                    403,
                    mapOf("error" to "Unauthorized").toJsonResponse(),
                )
            }
        }
        rule(
            get, url eq BASE_URL.plus("/api/v1/notifications/channels/transactions")
        ) {
            if (config.getNotificationChannelSuccess) {
                respond(
                    200,
                    NotificationChannelsResponse(
                        notificationChannels = listOf(
                            NotificationChannelResponse(
                                channelId = config.channelId,
                                smsEnabled = config.channelSmsEnabled,
                                emailEnabled = config.channelEmailEnabled,
                                pushEnabled = config.channelPushEnabled,
                            )
                        )
                    ).toJsonResponse(),
                )
            } else {
                respond(
                    403,
                    mapOf("error" to "Unauthorized").toJsonResponse(),
                )
            }
        }
        rule(
            get, url eq BASE_URL.plus("/api/v1/notifications/channels")
        ) {
            if (config.getNotificationChannelsSuccess) {
                respond(
                    200,
                    NotificationChannelsResponse(
                        notificationChannels = listOf(
                            NotificationChannelResponse(
                                channelId = config.channelId,
                                smsEnabled = config.channelSmsEnabled,
                                emailEnabled = config.channelEmailEnabled,
                                pushEnabled = config.channelPushEnabled,
                            )
                        )
                    ).toJsonResponse(),
                )
            } else {
                respond(
                    403,
                    mapOf("error" to "Unauthorized").toJsonResponse(),
                )
            }
        }
        rule(
            post, url eq BASE_URL.plus("/api/v1/notifications/channels/${config.channelId}")
        ) {
            if (config.postNotificationChannelSuccess) {
                respond(
                    200,
                    NotificationChannelResponse(
                        channelId = config.channelId,
                        smsEnabled = config.channelSmsEnabled,
                        emailEnabled = config.channelEmailEnabled,
                        pushEnabled = config.channelPushEnabled,
                    ).toJsonResponse(),
                )
            } else {
                respond(
                    403,
                    mapOf("error" to "Unauthorized").toJsonResponse(),
                )
            }
        }
    }

    private val mockOkHttpClient by lazy(LazyThreadSafetyMode.NONE) {
        OkHttpClient.Builder().addInterceptor { chain ->
            mockInterceptor().intercept(chain)
        }.build()
    }

    override val startup: Startup = Startup.Empty()

    override val notificationSettings: SettingsProvider =
        SettingsProvider.Preferences(MockPreferencesRepository())

    override val apiKeyHolder: HeaderApiKeyHolder =
        HeaderApiKeyHolder.X_API_TOKEN(settingsProvider = notificationSettings)

    override val notificationHttpService: NotificationHttpService by lazy(LazyThreadSafetyMode.NONE) {
        Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(ktxJson.asConverterFactory("application/json".toMediaType()))
            .client(mockOkHttpClient).build().create(NotificationHttpService::class.java)
    }

    override val notificationObserver: NotificationObserver =
        NotificationObserver.SSE(baseUrl = BASE_URL,
            path = "/api/v1/notifications/events",
            { chain -> chain.proceed(chain.request()) })
    override val validateUserData: ValidateUserData = ValidateUserData.Base()
}