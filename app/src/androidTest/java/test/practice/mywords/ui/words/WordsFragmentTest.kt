package test.practice.mywords.ui.words

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import test.practice.mywords.R
import test.practice.mywords.application.TestWordsApplication
import test.practice.mywords.data.FakeWordsRemoteDataSource
import test.practice.mywords.data.Word
import test.practice.mywords.data.WordsRepository
import test.practice.mywords.data.local.WordsDatabase
import test.practice.mywords.util.*

@RunWith(AndroidJUnit4::class)
@MediumTest
class WordsFragmentTest : KodeinAware {

    override val kodein by kodein {
        getApplicationContext<TestWordsApplication>()
    }

    private val database: WordsDatabase by instance()
    private val fakeWordsRemoteDataSource: FakeWordsRemoteDataSource by instance()
    private val repository: WordsRepository by instance()

    private val word1 = Word("word1")
    private val word2 = Word("word2")
    private val newWord = Word("newWord")

    @Before
    fun initRepository() {
        repository.deleteAllWordsBlocking()
    }

    @After
    fun reset() {
        resetRepository()
    }

    @Test
    fun loadWords_displaysWords() {
        // init repository with two words
        repository.saveWordBlocking(word1)
        repository.saveWordBlocking(word2)

        // start up Words screen
        launchFragmentInContainer<WordsFragment>(Bundle(), R.style.AppTheme)

        // verify that words are loaded in the list
        onView(withId(R.id.list_words))
            .check(matches(withItemCount(2)))
            .check(matches(withWord(word1, 0)))
            .check(matches(withWord(word2, 1)))
    }

    @Test
    fun clickAddNewWordButton_navigateToEditorFragment() {
        // start up Words screen
        val fragmentScenario = launchFragmentInContainer<WordsFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // click on add new word fab
        onView(withId(R.id.fab_add_new_word)).perform(click())

        // verify the user navigates to editor fragment
        verify(navController).navigate(
            WordsFragmentDirections.actionAddNewWord()
        )
    }

    @Test
    fun openWord_navigateToWordDetailFragment() {
        // init repository with 2 words
        repository.saveWordBlocking(word1)
        repository.saveWordBlocking(word2)

        // start up Words screen
        val fragmentScenario = launchFragmentInContainer<WordsFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // click on word in list to open in detail
        onView(withId(R.id.list_words))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(1))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(1, click()))

        // verify that the user is navigated to word details fragment
        verify(navController).navigate(
            WordsFragmentDirections.actionOpenWord(word2.word)
        )
    }

    @Test
    fun addNewWord_displaysNewWordInList() {
        // start up Words screen
        launch(WordsActivity::class.java)

        // click on add new word fab
        onView(withId(R.id.fab_add_new_word)).perform(click())

        // type new word in the edit text and save it
        onView(withId(R.id.input_word))
            .perform(typeText(newWord.word), closeSoftKeyboard())

        // click on save word fab
        onView(withId(R.id.fab_save_word)).perform(click())

        // verify that the new word is displayed in the list
        onView(withId(R.id.list_words))
            .check(matches(withWord(newWord, 0)))
    }

    @Test
    fun openWord_displaysSelectedWord() {
        // init repository with a word
        repository.saveWordBlocking(newWord)

        // start up Words screen
        launch(WordsActivity::class.java)

        // click on the word in the list
        onView(withId(R.id.list_words))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // verify that the selected word is shown
        onView(withId(R.id.display_word))
            .check(matches(withText(newWord.word)))
    }

    @Test
    fun deleteWord_deletesWordFromList() {
        // init repository with 2 words
        repository.saveWordBlocking(word1)
        repository.saveWordBlocking(word2)

        // start up Words screen
        launch(WordsActivity::class.java)

        // click on word in list
        onView(withId(R.id.list_words))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // click delete word menu
        onView(withId(R.id.action_delete)).perform(click())

        // verify the word was deleted
        onView(withId(R.id.list_words))
            .check(wordDoesNotExists(word1))
    }

    @Test
    fun deleteAllWords_listIsNotDisplayed() {
        // init repository with 2 words
        repository.saveWordBlocking(word1)
        repository.saveWordBlocking(word2)

        // start upp Words screen
        launch(WordsActivity::class.java)

        // click delete all words menu
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText(R.string.action_delete_all)).perform(click())

        // verify that list is not shown
        onView(withId(R.id.list_words))
            .check(matches(not(isDisplayed())))

        // verify that no words view is shown
        onView(withId(R.id.no_words))
            .check(matches(isDisplayed()))
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