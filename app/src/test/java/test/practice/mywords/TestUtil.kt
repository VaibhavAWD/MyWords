package test.practice.mywords

import androidx.lifecycle.LiveData
import org.junit.Assert
import test.practice.mywords.util.Event

fun <T: Any>assertLiveDataEventTriggered(liveData: LiveData<Event<T>>, expectedValue: T) {
    val value = LiveDataTestUtil.getValue(liveData)
    Assert.assertEquals(value.getContentIfNotHandled(), expectedValue)
}