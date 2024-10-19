package com.bank.notifications.presentation.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.bank.notifications.R
import com.bank.notifications.databinding.ActivityNotificationSettingsBinding
import com.bank.notifications.di.DI
import com.bank.notifications.domain.usecase.AuthUser
import com.bank.notifications.domain.usecase.FetchUserNotificationChannels
import com.bank.notifications.domain.usecase.FetchUserProfile
import com.bank.notifications.domain.usecase.UpdateUserNotificationChannel
import com.bank.notifications.domain.usecase.UpdateUserProfile
import com.bank.notifications.presentation.settings.NotificationSettingsState.DataState
import com.bank.notifications.presentation.settings.NotificationSettingsState.UiEffect
import com.bank.notifications.presentation.settings.component.EditType
import com.bank.notifications.presentation.settings.component.EditUserContactDialog
import com.bank.notifications.presentation.settings.recyclerview.NotificationListAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class NotificationSettingsActivity : AppCompatActivity(), EditUserContactDialog.EditListener {

    private val viewModel: NotificationSettingsViewModel by viewModels {
        NotificationSettingsViewModel.Factory(
            AuthUser(DI.notificationHttpService),
            FetchUserProfile(DI.notificationHttpService),
            UpdateUserProfile(DI.notificationHttpService),
            FetchUserNotificationChannels(DI.notificationHttpService),
            UpdateUserNotificationChannel(DI.notificationHttpService),
            DI.apiKeyHolder
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.activityToolbar.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.errorLayout.buttonRetry.setOnClickListener { viewModel.retryOnError() }
        binding.userEmailButton.setOnClickListener { viewModel.showEmailEditingDialog() }
        binding.userPhoneButton.setOnClickListener { viewModel.showPhoneEditingDialog() }

        val notificationsAdapter = NotificationListAdapter(
            onNotificationEnableChanged = viewModel::onNotificationEnableChanged
        ).apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        with(binding.notificationChannelList) {
            adapter = notificationsAdapter
            (itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        }

        viewModel.loadUserNotificationSettings()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiEffects.collectLatest { uiEffect ->
                        when (uiEffect) {
                            is UiEffect.ShowEditEmail -> showEditingDialog(
                                EditType.EMAIL, uiEffect.email
                            )

                            is UiEffect.ShowEditPhone -> showEditingDialog(
                                EditType.PHONE, uiEffect.phone
                            )

                            is UiEffect.ShowToast -> showToast(uiEffect.message)

                            UiEffect.CheckPushEnabled -> checkIfNotificationsPermissionGranted()

                            is UiEffect.RefreshListItem -> notificationsAdapter.notifyItemChanged(
                                uiEffect.itemId
                            )
                        }
                    }
                }
                launch {
                    viewModel.userState.collectLatest { userState ->
                        binding.userPhoneText.text = userState.userPhone
                        binding.userEmailText.text = userState.userEmail
                    }
                }
                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        val dataState = uiState.dataState

                        binding.loadingLayout.root.isVisible =
                            dataState is DataState.Loading && dataState.isLoading
                        binding.errorLayout.root.isVisible =
                            dataState is DataState.Error
                        if (dataState is DataState.Error) {
                            binding.errorLayout.errorText.text =
                                getString(
                                    R.string.error,
                                    dataState.error?.message ?: getString(R.string.unknown_error)
                                )
                        }

                        if (dataState is DataState.Data) {
                            notificationsAdapter.submitList(dataState.data)
                        }
                    }
                }
            }
        }
    }

    private fun checkIfNotificationsPermissionGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showToast(R.string.push_enabled_but_device_notifications_disabled)
        }
    }

    private fun showToast(@StringRes message: Int) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun showEditingDialog(type: EditType, value: String) {
        EditUserContactDialog.create(type, value)
            .show(supportFragmentManager, EditUserContactDialog.TAG)
    }

    override fun onUserContactEdited(editType: EditType, value: String) {
        when (editType) {
            EditType.PHONE -> viewModel.saveUserPhone(value)
            EditType.EMAIL -> viewModel.saveUserEmail(value)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
