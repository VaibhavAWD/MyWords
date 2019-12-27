package test.practice.mywords.ui.editor

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
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
import test.practice.mywords.data.Result.Success
import test.practice.mywords.data.Word
import test.practice.mywords.data.WordsRepository
import test.practice.mywords.data.local.WordsDatabase
import test.practice.mywords.util.deleteAllWordsBlocking
import test.practice.mywords.util.getWordsBlocking

@RunWith(AndroidJUnit4::class)
@MediumTest
class EditorFragmentTest : KodeinAware {

    override val kodein by kodein {
        getApplicationContext<TestWordsApplication>()
    }

    private val database: WordsDatabase by instance()
    private val fakeWordsRemoteDataSource: FakeWordsRemoteDataSource by instance()
    private val repository: WordsRepository by instance()

    private val newWord = Word("newWord")

    @Before
    fun initRepository() {
        repository.deleteAllWordsBlocking()
    }

    @After
    fun reset() {
        resetRepository()
    }

    /**
     * empty word _ is not saved
     */
    @Test
    fun emptyWord_isNotSave() {
        // start up "Add New Word" screen
        val navController = mock(NavController::class.java)
        launchFragment(navController)

        // enter invalid word in the text field
        onView(withId(R.id.input_word)).perform(clearText())

        // click save word fab
        onView(withId(R.id.fab_save_word)).perform(click())

        // verify that the text field is still being shown (a correct word would close it).
        onView(withId(R.id.input_word)).check(matches(isDisplayed()))
    }

    /**
     * valid word _ is saved
     */
    @Test
    fun validWord_isSaved() {
        // start up "Add New Word" screen
        val navController = mock(NavController::class.java)
        launchFragment(navController)

        // enter valid word in the text field
        onView(withId(R.id.input_word)).perform(replaceText(newWord.word))

        // click save word fab
        onView(withId(R.id.fab_save_word)).perform(click())

        // verify that the new word is saved in the repository
        val words = (repository.getWordsBlocking(true) as Success).data
        assertEquals(words.size, 1)
        assertEquals(words[0].word, newWord.word)
    }

    /**
     * valid word _ navigates back
     */
    @Test
    fun validWord_navigatesBack() {
        // start up "Add New Word" screen
        val navController = mock(NavController::class.java)
        launchFragment(navController)

        // enter valid word
        onView(withId(R.id.input_word)).perform(replaceText(newWord.word))

        // click on save word fab
        onView(withId(R.id.fab_save_word)).perform(click())

        // verify that user is redirected to the home screen
        verify(navController).navigate(
            EditorFragmentDirections.actionWordAdded()
        )
    }

    /**
     * Helper method to launch [EditorFragment].
     */
    private fun launchFragment(navController: NavController?) {
        val fragmentScenario = launchFragmentInContainer<EditorFragment>(Bundle(), R.style.AppTheme)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
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