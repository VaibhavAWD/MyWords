package test.practice.mywords.ui.detail

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import test.practice.mywords.R
import test.practice.mywords.application.TestWordsApplication
import test.practice.mywords.data.FakeWordsRemoteDataSource
import test.practice.mywords.data.Word
import test.practice.mywords.data.WordsRepository
import test.practice.mywords.data.local.WordsDatabase
import test.practice.mywords.util.deleteAllWordsBlocking
import test.practice.mywords.util.saveWordBlocking

@RunWith(AndroidJUnit4::class)
@MediumTest
class WordDetailFragmentTest : KodeinAware {

    override val kodein by kodein {
        getApplicationContext<TestWordsApplication>()
    }

    private val database: WordsDatabase by instance()
    private val fakeWordsRemoteDataSource: FakeWordsRemoteDataSource by instance()
    private val repository: WordsRepository by instance()

    private val word = Word("word")

    @Before
    fun initRepository() {
        repository.deleteAllWordsBlocking()
    }

    @After
    fun reset() {
        resetRepository()
    }

    /**
     * selected word _ is displayed
     */
    @Test
    fun selectedWord_isDisplayed() {
        // init repository with the word
        repository.saveWordBlocking(word)

        // start up "Word Detail" screen
        launchFragmentWithWord(word.word)

        // verify that the word is displayed
        onView(withId(R.id.display_word))
            .check(matches(isDisplayed()))
            .check(matches(withText(word.word)))
    }

    /**
     * invalid word _ no data available
     */
    @Test
    fun invalidWord_noDataAvailable() {
        // init repository with word
        repository.saveWordBlocking(word)

        // start up "Word Detail" screen
        launchFragmentWithWord("invalid_word")

        // verify that word is not displayed
        onView(withId(R.id.display_word))
            .check(matches(not(isDisplayed())))

        // verify that no data available view is shown
        onView(withId(R.id.no_data))
            .check(matches(isDisplayed()))
    }

    @Test
    fun emptyWord_noDataAvailable() {
        // start up "Word Detail" screen
        launchFragmentWithWord("")

        // verify that word is not displayed
        onView(withId(R.id.display_word))
            .check(matches(not(isDisplayed())))

        // verify that no data available view is shown
        onView(withId(R.id.no_data))
            .check(matches(isDisplayed()))
    }

    /**
     * Helper method to launch [WordDetailFragment] with bundle.
     */
    private fun launchFragmentWithWord(word: String) {
        val bundle = WordDetailFragmentArgs(word).toBundle()
        launchFragmentInContainer<WordDetailFragment>(bundle, R.style.AppTheme)
    }

    /**
     * Clean up data to avoid test pollution.
     */
    private fun resetRepository() {
        synchronized(this) {
            runBlocking {
                fakeWordsRemoteDataSource.deleteAllWords()
            }
            database.clearAllTables()
        }
    }
}