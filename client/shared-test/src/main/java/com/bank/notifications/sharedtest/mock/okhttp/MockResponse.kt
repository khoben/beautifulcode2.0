package com.bank.notifications.sharedtest.mock.okhttp

import okhttp3.mock.matchers.MethodMatcher

class MockResponse(
    val method: MethodMatcher,
    val path: String,
    val response: String,
    val status: Int = 200,
    val mediaType: String = "application/json"
)