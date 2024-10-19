package com.bank.notifications.di

import android.content.Context
import androidx.fragment.app.Fragment
import com.bank.notifications.App

val Context.DI: DiContainer get() = (applicationContext as App).appContainer
val Fragment.DI: DiContainer get() = requireContext().DI
