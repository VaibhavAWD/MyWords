package test.practice.mywords.util

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import test.practice.mywords.application.TestWordsApplication

class CustomTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, (TestWordsApplication::class.java).name, context)
    }
}