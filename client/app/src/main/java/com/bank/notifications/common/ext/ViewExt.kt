package com.bank.notifications.common.ext

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


fun View.ime(show: Boolean) {
    if (show) {
        requestFocus()
        ViewCompat.getWindowInsetsController(this)
            ?.show(WindowInsetsCompat.Type.ime())
    } else {
        clearFocus()
        ViewCompat.getWindowInsetsController(this)
            ?.hide(WindowInsetsCompat.Type.ime())
    }
}