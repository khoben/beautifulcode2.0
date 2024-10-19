package com.bank.notifications.testing.viewassertions

import android.view.View
import androidx.annotation.StringRes
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description

fun withTextLayoutHint(@StringRes hintId: Int?) =
    object : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {

        override fun matchesSafely(item: TextInputLayout?): Boolean =
            when {
                item == null -> false
                hintId == null -> item.hint == null
                else -> item.hint?.toString() == item.context.getString(hintId)
            }

        override fun describeTo(description: Description?) {
            description?.appendText("with hint id $hintId")
        }
    }