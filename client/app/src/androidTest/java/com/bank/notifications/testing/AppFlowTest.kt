package com.bank.notifications.testing

import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.intent.Intents
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.bank.notifications.TestApp
import com.bank.notifications.mock.MockDiConfig
import com.bank.notifications.mock.MockDiContainer
import com.bank.notifications.testing.idlingresource.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.reflect.KClass

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
abstract class AppFlowTest(
    private val clazz: KClass<out AppCompatActivity>,
    private val amountIdlingResources: Int = 0
) {

    val app = ApplicationProvider.getApplicationContext<TestApp>()
    val appContainer: MockDiContainer get() = app.appContainer as MockDiContainer

    @Before
    open fun before() {
        Intents.init()
        app.appContainer = MockDiContainer()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.idlingResource)
    }

    @After
    open fun after() {
        Intents.release()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.idlingResource)
    }

    fun launchWithIdling(scenarioScope: (ActivityScenario<out AppCompatActivity>) -> Unit) {
        waitForResources(amountIdlingResources)
        launch(scenarioScope)
    }

    fun launch(scenarioScope: (ActivityScenario<out AppCompatActivity>) -> Unit) {
        ActivityScenario.launch(clazz.java, null).use(scenarioScope)
    }

    private fun waitForResources(countResources: Int) {
        repeat(countResources) {
            EspressoIdlingResource.increment()
        }
    }

    inline fun updateDiConfig(crossinline block: MockDiConfig.() -> MockDiConfig) {
        (app.appContainer as MockDiContainer).config =
            (app.appContainer as MockDiContainer).config.block()
    }
}