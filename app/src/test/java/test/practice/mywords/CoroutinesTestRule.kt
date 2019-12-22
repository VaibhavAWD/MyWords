@file:Suppress("DEPRECATION")

package test.practice.mywords

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.concurrent.Executors
import kotlin.coroutines.ContinuationInterceptor

/**
 * Sets the main coroutines dispatcher for unit testing.
 *
 * Uses the deprecated TestCoroutineContext if provided. Otherwise it uses a new single thread
 * executor.
 */
@ExperimentalCoroutinesApi
class ViewModelScopeMainDispatcherRule(
    private val testContext: TestCoroutineContext? = null
) : TestWatcher() {

    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override fun starting(description: Description?) {
        super.starting(description)
        if (testContext != null) {
            Dispatchers.setMain(testContext[ContinuationInterceptor] as CoroutineDispatcher)
        } else {
            Dispatchers.setMain(singleThreadExecutor.asCoroutineDispatcher())
        }
    }

    override fun finished(description: Description?) {
        super.finished(description)
        singleThreadExecutor.shutdownNow()
        Dispatchers.resetMain()
    }
}