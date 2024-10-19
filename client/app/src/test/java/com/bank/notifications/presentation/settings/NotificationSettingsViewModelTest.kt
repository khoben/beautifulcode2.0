package com.bank.notifications.presentation.settings

import app.cash.turbine.turbineScope
import com.bank.notifications.data.cache.HeaderApiKeyHolder
import com.bank.notifications.data.cache.SettingsProvider
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.model.NotificationChannelDomain
import com.bank.notifications.domain.model.NotificationChannelDomainRequest
import com.bank.notifications.domain.model.NotificationChannelsDomain
import com.bank.notifications.domain.model.UserProfileDomain
import com.bank.notifications.domain.model.UserProfileDomainRequest
import com.bank.notifications.domain.model.UserTokenDomain
import com.bank.notifications.domain.usecase.UseCase
import com.bank.notifications.domain.usecase.UseCaseParam1
import com.bank.notifications.testing.BaseHttpServiceTest
import com.bank.notifications.testing.MockPreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NotificationSettingsViewModelTest : BaseHttpServiceTest() {

    private lateinit var mockSettingsManager: SettingsProvider
    private lateinit var headerApiKeyHolder: HeaderApiKeyHolder

    @Before
    fun setup() {
        mockSettingsManager = SettingsProvider.Preferences(
            preferencesRepository = MockPreferencesRepository(),
            apiEndpointBaseKey = "apiEndpointBaseKey",
            apiTokenKey = "apiTokenKey",
            defaultApiEndpointBase = "default_endpoint",
            defaultApiTokenValue = "default_token"
        )
        headerApiKeyHolder = HeaderApiKeyHolder.X_API_TOKEN(settingsProvider = mockSettingsManager)
    }

    private val DEFAULT_USE_CASES = mapOf(
        "authUser" to object : UseCase<UserTokenDomain> {
            override suspend fun invoke(): Flow<UseCaseResult<UserTokenDomain, HttpException>> =
                emptyFlow()
        },
        "fetchUserProfile" to object : UseCase<UserProfileDomain> {
            override suspend fun invoke(): Flow<UseCaseResult<UserProfileDomain, HttpException>> =
                emptyFlow()
        },
        "fetchUserNotificationChannels" to object : UseCase<NotificationChannelsDomain> {
            override suspend fun invoke(): Flow<UseCaseResult<NotificationChannelsDomain, HttpException>> =
                emptyFlow()
        },
        "updateUserProfile" to object : UseCaseParam1<UserProfileDomain, UserProfileDomainRequest> {
            override suspend fun invoke(input: UserProfileDomainRequest): Flow<UseCaseResult<UserProfileDomain, HttpException>> =
                emptyFlow()
        },
        "updateUserNotificationChannel" to object :
            UseCaseParam1<NotificationChannelDomain, NotificationChannelDomainRequest> {
            override suspend fun invoke(input: NotificationChannelDomainRequest): Flow<UseCaseResult<NotificationChannelDomain, HttpException>> =
                emptyFlow()
        }
    )

    private val DEFAULT_USECASE_ERROR = UseCaseResult.Error(
        HttpException(
            Response.error<Any>(
                403,
                "error".toResponseBody()
            )
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val DEFAULT_DISPATCHER = UnconfinedTestDispatcher()

    @Suppress("UNCHECKED_CAST")
    private fun createViewModel(
        useCases: Map<String, Any> = emptyMap(),
        dispatcher: CoroutineDispatcher = DEFAULT_DISPATCHER
    ): NotificationSettingsViewModel {
        val useCasesMap = DEFAULT_USE_CASES + useCases
        return NotificationSettingsViewModel(
            authUser = useCasesMap["authUser"] as UseCase<UserTokenDomain>,
            fetchUserProfile = useCasesMap["fetchUserProfile"] as UseCase<UserProfileDomain>,
            updateUserProfile = useCasesMap["updateUserProfile"] as UseCaseParam1<UserProfileDomain, UserProfileDomainRequest>,
            fetchUserNotificationChannels = useCasesMap["fetchUserNotificationChannels"] as UseCase<NotificationChannelsDomain>,
            updateUserNotificationChannel = useCasesMap["updateUserNotificationChannel"] as UseCaseParam1<NotificationChannelDomain, NotificationChannelDomainRequest>,
            apiKeyHolder = headerApiKeyHolder,
            ioDispatcher = dispatcher,
            mainDispatcher = dispatcher,
        )
    }

    @Test
    fun `should load user notifications settings when it is successful`() = runTest {

        val viewModel = createViewModel(
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            useCases = mapOf(
                "authUser" to object : UseCase<UserTokenDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<UserTokenDomain, HttpException>> {
                        return flow {
                            emit(UseCaseResult.Success(UserTokenDomain(token = "token")))
                        }
                    }
                },
                "fetchUserProfile" to object : UseCase<UserProfileDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                        return flow {
                            emit(
                                UseCaseResult.Success(
                                    UserProfileDomain(
                                        email = "test@test.com",
                                        phone = "1234567890"
                                    )
                                )
                            )
                        }
                    }
                },
                "fetchUserNotificationChannels" to object : UseCase<NotificationChannelsDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<NotificationChannelsDomain, HttpException>> {
                        return flow {
                            emit(
                                UseCaseResult.Success(
                                    NotificationChannelsDomain(
                                        listOf(
                                            NotificationChannelDomain(
                                                channelId = "channelId",
                                                smsEnabled = true,
                                                emailEnabled = true,
                                                pushEnabled = true,
                                            )
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
            )
        )

        turbineScope {

            val userStateReceiver = viewModel.userState.testIn(backgroundScope)
            val uiStateReceiver = viewModel.uiState.testIn(backgroundScope)

            viewModel.loadUserNotificationSettings()

            assertEquals(NotificationSettingsState.UserDataUi.EMPTY, userStateReceiver.awaitItem())
            assertEquals(
                NotificationSettingsState.UserDataUi(
                    userEmail = "test@test.com", userPhone = "1234567890"
                ), userStateReceiver.awaitItem()
            )

            assertEquals(NotificationSettingsState.UiState.EMPTY, uiStateReceiver.awaitItem())
            assertEquals(
                NotificationSettingsState.UiState(
                    dataState = NotificationSettingsState.DataState.Loading(
                        true
                    )
                ), uiStateReceiver.awaitItem()
            )
            assertEquals(
                NotificationSettingsState.UiState(
                    dataState = NotificationSettingsState.DataState.Data(
                        listOf(
                            NotificationSettingsState.ChannelDataUi(
                                channelId = "channelId",
                                smsEnabled = true,
                                emailEnabled = true,
                                pushEnabled = true
                            )
                        )
                    )
                ), uiStateReceiver.awaitItem()
            )
        }
    }

    @Test
    fun `should emit error state when received error from auth`() = runTest {

        val viewModel = createViewModel(
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            useCases = mapOf(
                "authUser" to object : UseCase<UserTokenDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<UserTokenDomain, HttpException>> {
                        return flow {
                            emit(DEFAULT_USECASE_ERROR)
                        }
                    }
                },
                "fetchUserProfile" to object : UseCase<UserProfileDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                        return flow {
                            emit(
                                UseCaseResult.Success(
                                    UserProfileDomain(
                                        email = "test@test.com",
                                        phone = "1234567890"
                                    )
                                )
                            )
                        }
                    }
                },
                "fetchUserNotificationChannels" to object : UseCase<NotificationChannelsDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<NotificationChannelsDomain, HttpException>> {
                        return flow {
                            emit(
                                UseCaseResult.Success(
                                    NotificationChannelsDomain(
                                        listOf(
                                            NotificationChannelDomain(
                                                channelId = "channelId",
                                                smsEnabled = true,
                                                emailEnabled = true,
                                                pushEnabled = true,
                                            )
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
            )
        )

        turbineScope {
            val uiStateReceiver = viewModel.uiState.testIn(backgroundScope)

            viewModel.loadUserNotificationSettings()

            assertEquals(NotificationSettingsState.UiState.EMPTY, uiStateReceiver.awaitItem())
            assertTrue(uiStateReceiver.awaitItem().dataState is NotificationSettingsState.DataState.Error)
        }
    }

    @Test
    fun `should emit error state when received error from user profile`() = runTest {
        val viewModel = createViewModel(
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            useCases = mapOf(
                "authUser" to object : UseCase<UserTokenDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<UserTokenDomain, HttpException>> {
                        return flow {
                            emit(UseCaseResult.Success(UserTokenDomain(token = "token")))
                        }
                    }
                },
                "fetchUserProfile" to object : UseCase<UserProfileDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                        return flow {
                            emit(DEFAULT_USECASE_ERROR)
                        }
                    }
                },
                "fetchUserNotificationChannels" to object : UseCase<NotificationChannelsDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<NotificationChannelsDomain, HttpException>> {
                        return flow {
                            emit(
                                UseCaseResult.Success(
                                    NotificationChannelsDomain(
                                        listOf(
                                            NotificationChannelDomain(
                                                channelId = "channelId",
                                                smsEnabled = true,
                                                emailEnabled = true,
                                                pushEnabled = true,
                                            )
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
            )
        )

        turbineScope {
            val uiStateReceiver = viewModel.uiState.testIn(backgroundScope)

            viewModel.loadUserNotificationSettings()

            assertEquals(NotificationSettingsState.UiState.EMPTY, uiStateReceiver.awaitItem())
            assertTrue(uiStateReceiver.awaitItem().dataState is NotificationSettingsState.DataState.Loading)
            assertTrue(uiStateReceiver.awaitItem().dataState is NotificationSettingsState.DataState.Error)
        }
    }

    @Test
    fun `should emit error state when received error from notification channels`() = runTest {
        val viewModel = createViewModel(
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            useCases = mapOf(
                "authUser" to object : UseCase<UserTokenDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<UserTokenDomain, HttpException>> {
                        return flow {
                            emit(UseCaseResult.Success(UserTokenDomain(token = "token")))
                        }
                    }
                },
                "fetchUserProfile" to object : UseCase<UserProfileDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                        return flow {
                            emit(
                                UseCaseResult.Success(
                                    UserProfileDomain(
                                        email = "test@test.com",
                                        phone = "1234567890"
                                    )
                                )
                            )
                        }
                    }
                },
                "fetchUserNotificationChannels" to object : UseCase<NotificationChannelsDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<NotificationChannelsDomain, HttpException>> {
                        return flow {
                            emit(DEFAULT_USECASE_ERROR)
                        }
                    }
                }
            )
        )

        turbineScope {
            val uiStateReceiver = viewModel.uiState.testIn(backgroundScope)

            viewModel.loadUserNotificationSettings()

            assertEquals(NotificationSettingsState.UiState.EMPTY, uiStateReceiver.awaitItem())
            assertTrue(uiStateReceiver.awaitItem().dataState is NotificationSettingsState.DataState.Loading)
            assertTrue(uiStateReceiver.awaitItem().dataState is NotificationSettingsState.DataState.Error)
        }
    }

    @Test
    fun `should save new user email when on success`() = runTest {
        val viewModel = createViewModel(
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            useCases = mapOf(
                "updateUserProfile" to object :
                    UseCaseParam1<UserProfileDomain, UserProfileDomainRequest> {
                    override suspend fun invoke(input: UserProfileDomainRequest): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                        return flow {
                            emit(
                                UseCaseResult.Success(
                                    UserProfileDomain(
                                        phone = "123456",
                                        email = "new_email@test.com"
                                    )
                                )
                            )
                        }
                    }
                }
            )
        )

        turbineScope {
            val userState = viewModel.userState.testIn(backgroundScope)
            userState.skipItems(1) // skip default state flow
            viewModel.saveUserEmail("new_email@test.com")
            assertEquals("new_email@test.com", userState.awaitItem().userEmail)
        }
    }

    @Test
    fun `should not save new email and emit error event when on error`() = runTest {

        val viewModel = createViewModel(
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            useCases = mapOf(
                "updateUserProfile" to object :
                    UseCaseParam1<UserProfileDomain, UserProfileDomainRequest> {
                    override suspend fun invoke(input: UserProfileDomainRequest): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                        return flow {
                            emit(DEFAULT_USECASE_ERROR)
                        }
                    }
                }
            )
        )

        turbineScope {
            val userState = viewModel.userState.testIn(backgroundScope)
            val eventState = viewModel.uiEffects.testIn(backgroundScope)
            viewModel.saveUserEmail("new_email@test.com")
            assertEquals("", userState.awaitItem().userEmail)
            assertTrue(eventState.awaitItem() is NotificationSettingsState.UiEffect.ShowToast)
        }
    }

    @Test
    fun `should save new user phone when on success`() = runTest {
        val viewModel = createViewModel(
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            useCases = mapOf(
                "updateUserProfile" to object :
                    UseCaseParam1<UserProfileDomain, UserProfileDomainRequest> {
                    override suspend fun invoke(input: UserProfileDomainRequest): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                        return flow {
                            emit(
                                UseCaseResult.Success(
                                    UserProfileDomain(
                                        phone = "new_phone",
                                        email = "test@test.com"
                                    )
                                )
                            )
                        }
                    }
                }
            )
        )

        turbineScope {
            val userState = viewModel.userState.testIn(backgroundScope)
            userState.skipItems(1) // skip default state flow
            viewModel.saveUserPhone("new_phone")
            assertEquals("new_phone", userState.awaitItem().userPhone)
        }
    }

    @Test
    fun `should not save new phone and emit error event when on error`() = runTest {

        val viewModel = createViewModel(
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            useCases = mapOf(
                "updateUserProfile" to object :
                    UseCaseParam1<UserProfileDomain, UserProfileDomainRequest> {
                    override suspend fun invoke(input: UserProfileDomainRequest): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                        return flow {
                            emit(DEFAULT_USECASE_ERROR)
                        }
                    }
                }
            )
        )

        turbineScope {
            val userState = viewModel.userState.testIn(backgroundScope)
            val eventState = viewModel.uiEffects.testIn(backgroundScope)
            viewModel.saveUserPhone("new_phone")
            assertEquals("", userState.awaitItem().userPhone)
            assertTrue(eventState.awaitItem() is NotificationSettingsState.UiEffect.ShowToast)
        }
    }

    @Test
    fun `should emit ShowEditEmail event when showEmailEditingDialog called`() = runTest {
        val viewModel = createViewModel(dispatcher = UnconfinedTestDispatcher(testScheduler))
        turbineScope {
            val eventState = viewModel.uiEffects.testIn(backgroundScope)
            viewModel.showEmailEditingDialog()
            assertTrue(eventState.awaitItem() is NotificationSettingsState.UiEffect.ShowEditEmail)
        }
    }

    @Test
    fun `should emit ShowEditPhone event when showEmailEditingDialog called`() = runTest {
        val viewModel = createViewModel(dispatcher = UnconfinedTestDispatcher(testScheduler))
        turbineScope {
            val eventState = viewModel.uiEffects.testIn(backgroundScope)
            viewModel.showPhoneEditingDialog()
            assertTrue(eventState.awaitItem() is NotificationSettingsState.UiEffect.ShowEditPhone)
        }
    }

    @Test
    fun `should emit successful state after error state when retryOnError called`() = runTest {
        val viewModel = createViewModel(
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            useCases = mapOf(
                "authUser" to object : UseCase<UserTokenDomain> {
                    private var emitError = true
                    override suspend fun invoke(): Flow<UseCaseResult<UserTokenDomain, HttpException>> {
                        return flow {
                            if (emitError) {
                                emitError = false
                                emit(DEFAULT_USECASE_ERROR)
                            } else {
                                emit(
                                    UseCaseResult.Success(UserTokenDomain(token = "token"))
                                )
                            }
                        }
                    }
                },
                "fetchUserProfile" to object : UseCase<UserProfileDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                        return flow {
                            emit(
                                UseCaseResult.Success(
                                    UserProfileDomain(
                                        email = "test@test.com",
                                        phone = "1234567890"
                                    )
                                )
                            )
                        }
                    }
                },
                "fetchUserNotificationChannels" to object : UseCase<NotificationChannelsDomain> {
                    override suspend fun invoke(): Flow<UseCaseResult<NotificationChannelsDomain, HttpException>> {
                        return flow {
                            emit(
                                UseCaseResult.Success(
                                    NotificationChannelsDomain(
                                        listOf(
                                            NotificationChannelDomain(
                                                channelId = "channelId",
                                                smsEnabled = true,
                                                emailEnabled = true,
                                                pushEnabled = true,
                                            )
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
            )
        )

        turbineScope {

            val userStateReceiver = viewModel.userState.testIn(backgroundScope)
            val uiStateReceiver = viewModel.uiState.testIn(backgroundScope)

            viewModel.loadUserNotificationSettings()

            assertEquals(NotificationSettingsState.UiState.EMPTY, uiStateReceiver.awaitItem())
            assertTrue(uiStateReceiver.awaitItem().dataState is NotificationSettingsState.DataState.Error)

            viewModel.retryOnError()

            assertEquals(NotificationSettingsState.UserDataUi.EMPTY, userStateReceiver.awaitItem())
            assertEquals(
                NotificationSettingsState.UserDataUi(
                    userEmail = "test@test.com", userPhone = "1234567890"
                ), userStateReceiver.awaitItem()
            )

            assertEquals(
                NotificationSettingsState.UiState(
                    dataState = NotificationSettingsState.DataState.Loading(
                        true
                    )
                ), uiStateReceiver.awaitItem()
            )
            assertEquals(
                NotificationSettingsState.UiState(
                    dataState = NotificationSettingsState.DataState.Data(
                        listOf(
                            NotificationSettingsState.ChannelDataUi(
                                channelId = "channelId",
                                smsEnabled = true,
                                emailEnabled = true,
                                pushEnabled = true
                            )
                        )
                    )
                ), uiStateReceiver.awaitItem()
            )
        }
    }

    @Test
    fun `should update notification channels state when onNotificationEnableChanged called with success`() =
        runTest {
            val viewModel = createViewModel(
                dispatcher = UnconfinedTestDispatcher(testScheduler),
                useCases = mapOf(
                    "authUser" to object : UseCase<UserTokenDomain> {
                        override suspend fun invoke(): Flow<UseCaseResult<UserTokenDomain, HttpException>> {
                            return flow {
                                emit(UseCaseResult.Success(UserTokenDomain(token = "token")))
                            }
                        }
                    },
                    "fetchUserProfile" to object : UseCase<UserProfileDomain> {
                        override suspend fun invoke(): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                            return flow {
                                emit(
                                    UseCaseResult.Success(
                                        UserProfileDomain(
                                            email = "test@test.com",
                                            phone = "1234567890"
                                        )
                                    )
                                )
                            }
                        }
                    },
                    "fetchUserNotificationChannels" to object :
                        UseCase<NotificationChannelsDomain> {
                        override suspend fun invoke(): Flow<UseCaseResult<NotificationChannelsDomain, HttpException>> {
                            return flow {
                                emit(
                                    UseCaseResult.Success(
                                        NotificationChannelsDomain(
                                            listOf(
                                                NotificationChannelDomain(
                                                    channelId = "channelId",
                                                    smsEnabled = true,
                                                    emailEnabled = true,
                                                    pushEnabled = true,
                                                )
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    },
                    "updateUserNotificationChannel" to object :
                        UseCaseParam1<NotificationChannelDomain, NotificationChannelDomainRequest> {
                        override suspend fun invoke(input: NotificationChannelDomainRequest): Flow<UseCaseResult<NotificationChannelDomain, HttpException>> {
                            return flow {
                                emit(
                                    UseCaseResult.Success(
                                        NotificationChannelDomain(
                                            input.channelId,
                                            input.smsEnabled,
                                            input.emailEnabled,
                                            input.pushEnabled
                                        )
                                    )
                                )
                            }
                        }
                    }
                )
            )

            turbineScope {

                val uiStateReceiver = viewModel.uiState.testIn(backgroundScope)
                val uiEventReceiver = viewModel.uiEffects.testIn(backgroundScope)

                viewModel.loadUserNotificationSettings()
                uiStateReceiver.skipItems(3) // skip initial load states

                viewModel.onNotificationEnableChanged(
                    0,
                    "channelId",
                    false /*updated value*/,
                    true,
                    true
                )
                // if push=true
                assertTrue(uiEventReceiver.awaitItem() is NotificationSettingsState.UiEffect.CheckPushEnabled)
                assertEquals(
                    NotificationSettingsState.UiState(
                        dataState = NotificationSettingsState.DataState.Data(
                            listOf(
                                NotificationSettingsState.ChannelDataUi(
                                    channelId = "channelId",
                                    smsEnabled = false, /*expect updated value*/
                                    emailEnabled = true,
                                    pushEnabled = true
                                )
                            )
                        )
                    ), uiStateReceiver.awaitItem()
                )
            }
        }

    @Test
    fun `should emit error event and not update notification channel when onNotificationEnableChanged called with fail`() =
        runTest {
            val viewModel = createViewModel(
                dispatcher = UnconfinedTestDispatcher(testScheduler),
                useCases = mapOf(
                    "authUser" to object : UseCase<UserTokenDomain> {
                        override suspend fun invoke(): Flow<UseCaseResult<UserTokenDomain, HttpException>> {
                            return flow {
                                emit(UseCaseResult.Success(UserTokenDomain(token = "token")))
                            }
                        }
                    },
                    "fetchUserProfile" to object : UseCase<UserProfileDomain> {
                        override suspend fun invoke(): Flow<UseCaseResult<UserProfileDomain, HttpException>> {
                            return flow {
                                emit(
                                    UseCaseResult.Success(
                                        UserProfileDomain(
                                            email = "test@test.com",
                                            phone = "1234567890"
                                        )
                                    )
                                )
                            }
                        }
                    },
                    "fetchUserNotificationChannels" to object :
                        UseCase<NotificationChannelsDomain> {
                        override suspend fun invoke(): Flow<UseCaseResult<NotificationChannelsDomain, HttpException>> {
                            return flow {
                                emit(
                                    UseCaseResult.Success(
                                        NotificationChannelsDomain(
                                            listOf(
                                                NotificationChannelDomain(
                                                    channelId = "channelId",
                                                    smsEnabled = true,
                                                    emailEnabled = true,
                                                    pushEnabled = true,
                                                )
                                            )
                                        )
                                    )
                                )
                            }
                        }
                    },
                    "updateUserNotificationChannel" to object :
                        UseCaseParam1<NotificationChannelDomain, NotificationChannelDomainRequest> {
                        override suspend fun invoke(input: NotificationChannelDomainRequest): Flow<UseCaseResult<NotificationChannelDomain, HttpException>> {
                            return flow {
                                emit(DEFAULT_USECASE_ERROR)
                            }
                        }
                    }
                )
            )

            turbineScope {

                val uiStateReceiver = viewModel.uiState.testIn(backgroundScope)
                val uiEventReceiver = viewModel.uiEffects.testIn(backgroundScope)

                viewModel.loadUserNotificationSettings()
                uiStateReceiver.skipItems(3) // skip initial load states

                viewModel.onNotificationEnableChanged(
                    0,
                    "channelId",
                    false /*updated value*/,
                    true,
                    true
                )
                // if push=true
                assertTrue(uiEventReceiver.awaitItem() is NotificationSettingsState.UiEffect.CheckPushEnabled)
                // on fail
                assertTrue(uiEventReceiver.awaitItem() is NotificationSettingsState.UiEffect.RefreshListItem)
                assertTrue(uiEventReceiver.awaitItem() is NotificationSettingsState.UiEffect.ShowToast)
                // no updates on fail
                assertTrue(uiStateReceiver.asChannel().isEmpty)
            }
        }
}