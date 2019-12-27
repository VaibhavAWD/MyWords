package test.practice.mywords.ui.words

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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
import test.practice.mywords.util.*

/**
 * Large End-to-End test for the words module.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class WordsActivityTest : KodeinAware {

    override val kodein by kodein {
        getApplicationContext<TestWordsApplication>()
    }
    private val database: WordsDatabase by instance()
    private val fakeWordsRemoteDataSource: FakeWordsRemoteDataSource by instance()
    private val repository: WordsRepository by instance()

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private val word1 = Word("word1")
    private val word2 = Word("word2")
    private val newWord = Word("newWord")

    @Before
    fun init() {
        repository.deleteAllWordsBlocking()
    }

    @After
    fun reset() {
        resetRepository()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun loadWords_displaysWords() {
        // init repository with two words
        repository.saveWordBlocking(word1)
        repository.saveWordBlocking(word2)

        // start up Words screen
        val activityScenario = ActivityScenario.launch(WordsActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // verify RecyclerView is displayed
        onView(withId(R.id.list_words)).check(matches(isDisplayed()))

        // verify list contains two items
        onView(withId(R.id.list_words))
            .check(matches(withItemCount(2)))
            // verify list contains the given words
            .check(matches(withWord(word1, 0)))
            .check(matches(withWord(word2, 1)))
    }

    @Test
    fun addNewWord_displaysNewWordInList() {
        // start up Words screen
        val activityScenario = ActivityScenario.launch(WordsActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // click '+' fab to add new word
        onView(withId(R.id.fab_add_new_word))
            .perform(click())

        // create new word
        onView(withId(R.id.input_word))
            .perform(typeText(newWord.word), closeSoftKeyboard())
        onView(withId(R.id.fab_save_word))
            .perform(click())

        // verify new word is displayed in the list
        onView(withId(R.id.list_words))
            .check(matches(withWord(newWord, 0)))
    }

    @Test
    fun openWord_displaysWordInDetails() {
        // init repository with two words
        repository.saveWordBlocking(word1)
        repository.saveWordBlocking(word2)

        // start up Words screen
        val activityScenario = ActivityScenario.launch(WordsActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // click on the word in the list to open in detail
        onView(withId(R.id.list_words))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // verify correct word is shown in detail
        onView(withId(R.id.display_word))
            .check(matches(withText(word2.word)))
    }

    @Test
    fun deleteWord_deletesWordFromList() {
        // init repository with two words
        repository.saveWordBlocking(word1)
        repository.saveWordBlocking(word2)

        // start up Words screen
        val activityScenario = ActivityScenario.launch(WordsActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // click on the word in the list
        onView(withId(R.id.list_words))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // click delete word in menu
        onView(withId(R.id.action_delete)).perform(click())

        // verify only one word was deleted
        onView(withId(R.id.list_words))
            .check(matches(withItemCount(1)))
            .check(wordDoesNotExists(word2))
    }

    /**
     * delete all words _ deletes all words
     */
    @Test
    fun deleteAllWords_listIsNotDisplayed() {
        // init repository with two words
        repository.saveWordBlocking(word1)
        repository.saveWordBlocking(word2)

        // start up Words screen
        val activityScenario = ActivityScenario.launch(WordsActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // before deleting the words verify the list is displayed
        onView(withId(R.id.list_words))
            .check(matches(isDisplayed()))
            .check(matches(withItemCount(2)))

        // open overflow menu
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        // click delete all words menu
        onView(withText(R.string.action_delete_all))
            .perform(click())

        // verify that list is not displayed
        onView(withId(R.id.list_words))
            .check(matches(not(isDisplayed())))

        // verify that the no words view is shown
        onView(withId(R.id.no_words))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.no_words)))
    }

    /**
     * Clears all data to avoid the test pollution.
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