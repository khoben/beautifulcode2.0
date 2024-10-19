package com.bank.notifications.domain

interface ValidateUserData {
    fun isEmailValid(email: String?): Boolean
    fun isPhoneValid(phone: String?): Boolean

    class Base : ValidateUserData {
        override fun isEmailValid(email: String?): Boolean {
            return !email.isNullOrBlank() && EMAIL_REGEX.matches(email)
        }

        override fun isPhoneValid(phone: String?): Boolean {
            return !phone.isNullOrBlank() && phone.length in 9..18 && PHONE_REGEX.matches(phone)
        }

        companion object {
            private val EMAIL_REGEX = Regex(
                "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})"
            )
            private val PHONE_REGEX = Regex(
                "(\\+[0-9]+[\\- .]*)?(\\([0-9]+\\)[\\- .]*)?([0-9][0-9\\- .]+[0-9])"
            )
        }
    }
}