package com.bank.notifications.di

import android.content.Context
import com.bank.notifications.BuildConfig
import com.bank.notifications.common.startup.Startup
import com.bank.notifications.data.cache.HeaderApiKeyHolder
import com.bank.notifications.data.cache.PreferencesRepository
import com.bank.notifications.data.cache.SettingsProvider
import com.bank.notifications.data.cloud.NotificationHttpService
import com.bank.notifications.domain.ValidateUserData
import com.bank.notifications.service.notificationobverver.NotificationObserver
import com.bank.notifications.service.platform.NotificationPushService
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(private val context: Context) : DiContainer {

    companion object {
        private const val CONNECTION_TIMEOUT_MS = 10_000L
        private const val READ_TIMEOUT_MS = 10_000L
        private const val WRITE_TIMEOUT_MS = 10_000L
    }

    private val ktxJson by lazy(LazyThreadSafetyMode.NONE) { Json { ignoreUnknownKeys = true } }

    private fun baseRetrofitBuilder(baseUrl: String) = Retrofit.Builder().baseUrl(baseUrl)
        .addConverterFactory(ktxJson.asConverterFactory("application/json".toMediaType()))

    private fun apiKeyInterceptor(apiKeyHolder: HeaderApiKeyHolder): Interceptor =
        Interceptor { chain ->
            var original = chain.request()
            val headers =
                original.headers.newBuilder().add(apiKeyHolder.header, apiKeyHolder.value).build()
            original = original.newBuilder().headers(headers).build()
            chain.proceed(original)
        }

    private fun hostInterceptor(settingsProvider: SettingsProvider): Interceptor =
        Interceptor { chain ->
            var original = chain.request()
            val baseApiUrlParts = settingsProvider.apiEndpointBase.split("://")
            if (baseApiUrlParts.size != 2) {
                return@Interceptor chain.proceed(original)
            }
            val (scheme, host) = baseApiUrlParts
            val newUrl = original.url.newBuilder().scheme(scheme).host(host).build()
            original = original.newBuilder().url(newUrl).build()
            chain.proceed(original)
        }

    private val baseOkHttp: OkHttpClient by lazy(LazyThreadSafetyMode.NONE) {
        OkHttpClient.Builder().connectTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS).build()
    }

    override val startup: Startup
        get() = Startup.Group(listOf(NotificationPushService))

    override val notificationSettings: SettingsProvider by lazy(LazyThreadSafetyMode.NONE) {
        SettingsProvider.Preferences(
            preferencesRepository = PreferencesRepository.SharedPreferencesRepository(
                context = context,
                name = "NotificationPrefs"
            )
        )
    }

    override val apiKeyHolder: HeaderApiKeyHolder by lazy(LazyThreadSafetyMode.NONE) {
        HeaderApiKeyHolder.X_API_TOKEN(settingsProvider = notificationSettings)
    }

    override val notificationHttpService: NotificationHttpService by lazy(LazyThreadSafetyMode.NONE) {
        baseRetrofitBuilder(notificationSettings.apiEndpointBase).client(
            baseOkHttp.newBuilder()
                .addInterceptor(hostInterceptor(notificationSettings))
                .addInterceptor(apiKeyInterceptor(apiKeyHolder)).apply {
                    if (BuildConfig.DEBUG) addInterceptor(
                        HttpLoggingInterceptor().setLevel(
                            HttpLoggingInterceptor.Level.HEADERS
                        )
                    )
                }.build()
        ).build().create(NotificationHttpService::class.java)
    }

    override val notificationObserver: NotificationObserver
        get() = NotificationObserver.SSE(
            baseUrl = notificationSettings.apiEndpointBase,
            path = "/api/v1/notifications/events",
            interceptor = apiKeyInterceptor(apiKeyHolder)
        )

    override val validateUserData: ValidateUserData
        get() = ValidateUserData.Base()
}