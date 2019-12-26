package test.practice.mywords.application

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import test.practice.mywords.BuildConfig
import test.practice.mywords.data.DefaultWordsRepository
import test.practice.mywords.data.WordsRepository
import test.practice.mywords.data.local.WordsDao
import test.practice.mywords.data.local.WordsDatabase
import test.practice.mywords.data.local.WordsLocalDataSource
import test.practice.mywords.data.remote.WordsRemoteDataSource
import test.practice.mywords.util.ViewModelFactory
import timber.log.Timber

@Suppress("unused")
open class WordsApplication : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@WordsApplication))
        bind() from singleton { WordsDatabase.getInstance(instance()) }
        bind<WordsDao>() with singleton { instance<WordsDatabase>().wordsDao() }
        bind() from singleton { WordsLocalDataSource(instance()) }
        bind() from singleton { WordsRemoteDataSource() }
        bind<WordsRepository>() with singleton { DefaultWordsRepository(instance(), instance()) }
        bind() from provider { ViewModelFactory(instance()) }
    }

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