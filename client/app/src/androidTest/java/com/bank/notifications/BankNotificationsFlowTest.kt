package com.bank.notifications

import android.os.Build
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotEnabled
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.GrantPermissionRule
import com.bank.notifications.presentation.MainActivity
import com.bank.notifications.presentation.settings.NotificationSettingsActivity
import com.bank.notifications.testing.AppFlowTest
import com.bank.notifications.testing.viewassertions.RecyclerViewItemCountAssertion
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test

class BankNotificationsFlowTest : AppFlowTest(MainActivity::class) {

    @get:Rule
    val permissionRule: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) GrantPermissionRule.grant(
            android.Manifest.permission.POST_NOTIFICATIONS
        ) else GrantPermissionRule.grant()

    @Test
    fun ensureThatMainActivityIsLaunched() {
        launch {
            intended(hasComponent(MainActivity::class.java.getName()))
        }
    }

    @Test
    fun shouldShowNotificationsSettingsButton() {
        launch {
            onView(withId(R.id.button_open_settings)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldLaunchNotificationsSettingsActivityWhenSettingsButtonClicked() {
        launch {
            onView(withId(R.id.button_open_settings)).perform(click())
            intended(hasComponent(NotificationSettingsActivity::class.java.getName()))
        }
    }

    @Test
    fun shouldShowLayoutNotificationsSettingsActivityWhenSettingsButtonClickedWithSuccessResponse() {
        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.user_email_text)).check(matches(isDisplayed()))
            onView(withId(R.id.user_email_button)).check(matches(isDisplayed()))
            onView(withId(R.id.user_phone_text)).check(matches(isDisplayed()))
            onView(withId(R.id.user_phone_button)).check(matches(isDisplayed()))
            onView(withId(R.id.notification_channel_list)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldShowDataNotificationsSettingsActivityWhenSettingsButtonClickedWithSuccessResponse() {
        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.user_email_text)).check(matches(withText(appContainer.config.userEmail)))
            onView(withId(R.id.user_phone_text)).check(matches(withText(appContainer.config.userPhone)))
            onView(withId(R.id.notification_channel_list)).check(RecyclerViewItemCountAssertion(1))
        }
    }

    @Test
    fun shouldShowErrorNotificationsSettingsActivityWhenSettingsButtonClickedWithFailedAuthResponse() {

        updateDiConfig {
            copy(authSuccess = false)
        }

        launch {
            onView(withId(R.id.button_open_settings)).perform(click())
            onView(withId(R.id.error_layout)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldShowErrorNotificationsSettingsActivityWhenSettingsButtonClickedWithFailedProfileResponse() {

        updateDiConfig {
            copy(getUserProfileSuccess = false)
        }

        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.error_layout)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldShowErrorNotificationsSettingsActivityWhenSettingsButtonClickedWithFailedChannelsResponse() {

        updateDiConfig {
            copy(getNotificationChannelsSuccess = false)
        }

        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.error_layout)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun shouldReloadWhenRetryButtonClicked() {

        updateDiConfig {
            copy(getNotificationChannelsSuccess = false)
        }

        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.error_layout)).check(matches(isDisplayed()))

            updateDiConfig {
                copy(getNotificationChannelsSuccess = true)
            }

            onView(withId(R.id.button_retry)).perform(click())

            onView(withId(R.id.error_layout)).check(matches(not(isDisplayed())))

            onView(withId(R.id.notification_channel_list)).check(RecyclerViewItemCountAssertion(1))
            onView(withId(R.id.user_email_text)).check(matches(withText(appContainer.config.userEmail)))
            onView(withId(R.id.user_phone_text)).check(matches(withText(appContainer.config.userPhone)))
        }
    }

    @Test
    fun shouldShowErrorStateWhenInvalidPhoneInput() {
        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.user_phone_button)).perform(click())

            onView(withId(R.id.user_contact_input)).perform(typeText("new_phone"))

            onView(withId(R.id.button_save)).check(matches(isNotEnabled()))
        }
    }

    @Test
    fun shouldShowErrorStateWhenInvalidEmailInput() {
        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.user_email_button)).perform(click())

            onView(withId(R.id.user_contact_input)).perform(typeText("new_email"))

            onView(withId(R.id.button_save)).check(matches(isNotEnabled()))
        }
    }

    @Test
    fun shouldChangeUserPhoneWhenSuccessResponse() {

        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.user_phone_button)).perform(click())

            // Dialog
            onView(withId(R.id.user_contact_input_layout)).check(matches(isDisplayed()))
            onView(withId(R.id.user_contact_input)).check(matches(withText(appContainer.config.userPhone)))
            onView(withId(R.id.button_save)).check(matches(isDisplayed()))

            // Save and close dialog
            onView(isRoot()).perform(closeSoftKeyboard())

            updateDiConfig { copy(userPhone = "new_phone") }

            onView(withId(R.id.button_save)).perform(click())
            onView(withId(R.id.user_contact_input_layout)).check(doesNotExist())

            // Check new phone
            onView(withId(R.id.user_phone_text)).check(matches(withText("new_phone")))
        }
    }

    @Test
    fun shouldChangeUserEmailWhenSuccessResponse() {

        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.user_email_button)).perform(click())

            // Dialog
            onView(withId(R.id.user_contact_input_layout)).check(matches(isDisplayed()))
            onView(withId(R.id.user_contact_input)).check(matches(withText(appContainer.config.userEmail)))
            onView(withId(R.id.button_save)).check(matches(isDisplayed()))

            // Save and close dialog
            onView(isRoot()).perform(closeSoftKeyboard())

            updateDiConfig { copy(userEmail = "new@email.com") }

            onView(withId(R.id.button_save)).perform(click())
            onView(withId(R.id.user_contact_input_layout)).check(doesNotExist())

            // Check new email
            onView(withId(R.id.user_email_text)).check(matches(withText("new@email.com")))
        }
    }

    @Test
    fun shouldNotChangeUserPhoneWhenFailedResponse() {

        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.user_phone_button)).perform(click())

            // Dialog
            onView(withId(R.id.user_contact_input_layout)).check(matches(isDisplayed()))
            onView(withId(R.id.user_contact_input)).check(matches(withText(appContainer.config.userPhone)))
            onView(withId(R.id.button_save)).check(matches(isDisplayed()))

            // Save and close dialog
            onView(isRoot()).perform(closeSoftKeyboard())

            val oldPhone = appContainer.config.userPhone
            updateDiConfig { copy(postUserProfileSuccess = false, userPhone = "new_phone") }

            onView(withId(R.id.button_save)).perform(click())
            onView(withId(R.id.user_contact_input_layout)).check(doesNotExist())

            // Check old phone
            onView(withId(R.id.user_phone_text)).check(matches(withText(oldPhone)))
        }
    }

    @Test
    fun shouldNotChangeUserEmailWhenFailedResponse() {

        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.user_email_button)).perform(click())

            // Dialog
            onView(withId(R.id.user_contact_input_layout)).check(matches(isDisplayed()))
            onView(withId(R.id.user_contact_input)).check(matches(withText(appContainer.config.userEmail)))
            onView(withId(R.id.button_save)).check(matches(isDisplayed()))

            // Save and close dialog
            onView(isRoot()).perform(closeSoftKeyboard())

            val oldEmail = appContainer.config.userEmail
            updateDiConfig { copy(postUserProfileSuccess = false, userEmail = "new@email.com") }

            onView(withId(R.id.button_save)).perform(click())
            onView(withId(R.id.user_contact_input_layout)).check(doesNotExist())

            // Check new email
            onView(withId(R.id.user_email_text)).check(matches(withText(oldEmail)))
        }
    }

    @Test
    fun shouldUpdateChannelWhenSuccessResponse() {

        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.notification_channel_list)).check(
                RecyclerViewItemCountAssertion(
                    1
                )
            )

            updateDiConfig { copy(channelEmailEnabled = false) }
            onView(withId(R.id.check_email)).perform(click())

            onView(withId(R.id.check_email)).check(matches(not(isChecked())))
        }
    }

    @Test
    fun shouldNotUpdateChannelWhenFailedResponse() {

        launch {
            onView(withId(R.id.button_open_settings)).perform(click())

            onView(withId(R.id.notification_channel_list)).check(
                RecyclerViewItemCountAssertion(
                    1
                )
            )

            updateDiConfig { copy(postNotificationChannelSuccess = false) }

            onView(withId(R.id.check_email)).perform(click())

            onView(withId(R.id.check_email)).check(matches(isChecked()))
        }
    }
}