package com.bank.notifications.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bank.notifications.common.ext.ime
import com.bank.notifications.common.ext.maybeRequestPostNotifications
import com.bank.notifications.databinding.ActivityMainBinding
import com.bank.notifications.di.DI
import com.bank.notifications.presentation.settings.NotificationSettingsActivity

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(DI.notificationSettings)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.activityToolbar.toolbar)

        viewModel.loadApiEndpoint { binding.apiAddressInput.setText(it) }

        binding.buttonApiEndpointSave.setOnClickListener {
            binding.apiAddressInput.ime(show = false)
            viewModel.storeApiEndpoint(binding.apiAddressInput.text.toString())
        }
        binding.buttonOpenSettings.setOnClickListener { openNotificationSettings() }

        maybeRequestPostNotifications()
    }

    private fun openNotificationSettings() {
        startActivity(
            Intent(
                this, NotificationSettingsActivity::class.java
            )
        )
    }
}