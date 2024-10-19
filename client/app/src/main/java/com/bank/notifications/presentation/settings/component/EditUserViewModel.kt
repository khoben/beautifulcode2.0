package com.bank.notifications.presentation.settings.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bank.notifications.domain.ValidateUserData

class EditUserViewModel(private val validateUserData: ValidateUserData) : ViewModel() {

    fun validate(type: EditType, input: String): Boolean {
        return when (type) {
            EditType.PHONE -> validateUserData.isPhoneValid(input)
            EditType.EMAIL -> validateUserData.isEmailValid(input)
        }
    }

    class Factory(
        private val validateUserData: ValidateUserData
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditUserViewModel(validateUserData) as T
        }
    }
}