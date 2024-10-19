package com.bank.notifications.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bank.notifications.R
import com.bank.notifications.common.ext.replaceFirstIf
import com.bank.notifications.data.cache.HeaderApiKeyHolder
import com.bank.notifications.domain.common.UseCaseResult
import com.bank.notifications.domain.common.chain
import com.bank.notifications.domain.common.combine
import com.bank.notifications.domain.common.fold
import com.bank.notifications.domain.model.NotificationChannelDomain
import com.bank.notifications.domain.model.NotificationChannelDomainRequest
import com.bank.notifications.domain.model.NotificationChannelsDomain
import com.bank.notifications.domain.model.UserProfileDomain
import com.bank.notifications.domain.model.UserProfileDomainRequest
import com.bank.notifications.domain.model.UserTokenDomain
import com.bank.notifications.domain.usecase.UseCase
import com.bank.notifications.domain.usecase.UseCaseParam1
import com.bank.notifications.presentation.settings.NotificationSettingsState.ChannelDataUi
import com.bank.notifications.presentation.settings.NotificationSettingsState.DataState
import com.bank.notifications.presentation.settings.NotificationSettingsState.UiEffect
import com.bank.notifications.presentation.settings.NotificationSettingsState.UiState
import com.bank.notifications.presentation.settings.NotificationSettingsState.UserDataUi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(
    private val authUser: UseCase<UserTokenDomain>,
    private val fetchUserProfile: UseCase<UserProfileDomain>,
    private val updateUserProfile: UseCaseParam1<UserProfileDomain, UserProfileDomainRequest>,
    private val fetchUserNotificationChannels: UseCase<NotificationChannelsDomain>,
    private val updateUserNotificationChannel: UseCaseParam1<NotificationChannelDomain, NotificationChannelDomainRequest>,
    private val apiKeyHolder: HeaderApiKeyHolder,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _userState = MutableStateFlow(UserDataUi.EMPTY)
    val userState = _userState.asStateFlow()

    private val _uiState = MutableStateFlow(UiState.EMPTY)
    val uiState = _uiState.asStateFlow()

    private val _uiEffects: Channel<UiEffect> = Channel(Channel.BUFFERED)
    val uiEffects = _uiEffects.receiveAsFlow()

    private var dataLoadingTriggered = false

    fun loadUserNotificationSettings(force: Boolean = false) {
        if (dataLoadingTriggered && !force) return

        dataLoadingTriggered = true

        viewModelScope.launch(mainDispatcher) {
            chain(
                authUser().onEach { auth ->
                    auth.fold(onSuccess = {
                        apiKeyHolder.value = it.token
                    })
                }, combine(fetchUserProfile(), fetchUserNotificationChannels(), ::Pair)
            ) { _, userAndChannels ->
                userAndChannels
            }.flowOn(ioDispatcher).onStart {
                _uiState.update {
                    it.copy(dataState = DataState.Loading(true))
                }
            }.catch { error ->
                _uiState.update {
                    it.copy(dataState = DataState.Error(error))
                }
            }.collectLatest { result ->
                result.fold(onSuccess = { data ->
                    val (user, channels) = data
                    _userState.update {
                        it.copy(userPhone = user.phone, userEmail = user.email)
                    }
                    val channelList = channels.notificationChannels.map(ChannelDataUi::fromDomain)
                    _uiState.update { state ->
                        state.copy(
                            dataState = DataState.Data(channelList)
                        )
                    }
                }, onError = { error ->
                    _uiState.update { state ->
                        state.copy(
                            dataState = DataState.Error(error)
                        )
                    }
                })
            }
        }
    }

    fun saveUserPhone(phone: String) = saveUserProfile(UserProfileDomainRequest(phone = phone))

    fun saveUserEmail(email: String) = saveUserProfile(UserProfileDomainRequest(email = email))

    private fun saveUserProfile(userProfileDomain: UserProfileDomainRequest) {
        viewModelScope.launch(mainDispatcher) {
            updateUserProfile(input = userProfileDomain).flowOn(ioDispatcher).catch {
                _uiEffects.send(
                    UiEffect.ShowToast(R.string.error_while_saving)
                )
            }.collectLatest { result ->
                when (result) {
                    is UseCaseResult.Success -> {
                        _userState.update { state ->
                            state.copy(
                                userEmail = result.data.email, userPhone = result.data.phone
                            )
                        }
                    }

                    is UseCaseResult.Error -> {
                        _uiEffects.send(UiEffect.ShowToast(R.string.error_while_saving))
                    }
                }
            }
        }
    }

    fun showEmailEditingDialog() {
        viewModelScope.launch(mainDispatcher) {
            _uiEffects.send(UiEffect.ShowEditEmail(_userState.value.userEmail))
        }
    }

    fun showPhoneEditingDialog() {
        viewModelScope.launch(mainDispatcher) {
            _uiEffects.send(UiEffect.ShowEditPhone(_userState.value.userPhone))
        }
    }

    fun retryOnError() = loadUserNotificationSettings(force = true)

    fun onNotificationEnableChanged(
        itemListIdx: Int,
        channelId: String,
        smsEnabled: Boolean,
        emailEnabled: Boolean,
        pushEnabled: Boolean
    ) {
        viewModelScope.launch(mainDispatcher) {
            if (pushEnabled) {
                _uiEffects.send(UiEffect.CheckPushEnabled)
            }
            updateUserNotificationChannel(
                input = NotificationChannelDomainRequest(
                    channelId = channelId,
                    smsEnabled = smsEnabled,
                    emailEnabled = emailEnabled,
                    pushEnabled = pushEnabled,
                )
            ).flowOn(ioDispatcher).catch {
                _uiEffects.send(UiEffect.RefreshListItem(itemListIdx))
                _uiEffects.send(UiEffect.ShowToast(R.string.error_while_saving))
            }.collectLatest { result ->
                when (result) {
                    is UseCaseResult.Success -> {
                        val dataState = _uiState.value.dataState
                        if (dataState !is DataState.Data) return@collectLatest
                        val updatedChannel = result.data
                        val updatedChannelList =
                            dataState.data.replaceFirstIf(predicate = { it.channelId == updatedChannel.channelId },
                                factory = { ChannelDataUi.fromDomain(updatedChannel) })
                        _uiState.update {
                            it.copy(
                                dataState = DataState.Data(updatedChannelList)
                            )
                        }
                    }

                    is UseCaseResult.Error -> {
                        _uiEffects.send(UiEffect.RefreshListItem(itemListIdx))
                        _uiEffects.send(UiEffect.ShowToast(R.string.error_while_saving))
                    }
                }
            }
        }
    }

    class Factory(
        private val authUser: UseCase<UserTokenDomain>,
        private val fetchUserProfile: UseCase<UserProfileDomain>,
        private val updateUserProfile: UseCaseParam1<UserProfileDomain, UserProfileDomainRequest>,
        private val fetchUserNotificationChannels: UseCase<NotificationChannelsDomain>,
        private val updateUserNotificationChannel: UseCaseParam1<NotificationChannelDomain, NotificationChannelDomainRequest>,
        private val apiKeyHolder: HeaderApiKeyHolder,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationSettingsViewModel(
                authUser,
                fetchUserProfile,
                updateUserProfile,
                fetchUserNotificationChannels,
                updateUserNotificationChannel,
                apiKeyHolder,
            ) as T
        }
    }
}