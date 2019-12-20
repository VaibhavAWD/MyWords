package test.practice.mywords.application

import android.app.Application
import test.practice.mywords.BuildConfig
import timber.log.Timber

@Suppress("unused")
class WordsApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}