package com.bank.notifications

import com.bank.notifications.di.DiContainer
import com.bank.notifications.mock.MockDiContainer

class TestApp : App() {
    override var appContainer: DiContainer = MockDiContainer()
}