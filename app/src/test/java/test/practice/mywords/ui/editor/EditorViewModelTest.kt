@file:Suppress("DEPRECATION")

package test.practice.mywords.ui.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import test.practice.mywords.R
import test.practice.mywords.ViewModelScopeMainDispatcherRule
import test.practice.mywords.assertLiveDataEventTriggered
import test.practice.mywords.data.FakeRepository

/**
 * Unit tests for the implementation of [EditorViewModel].
 */
class EditorViewModelTest {

    // SUT
    private lateinit var editorViewModel: EditorViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var wordsRepository: FakeRepository

    // A CoroutineContext that can be controlled from tests
    private val testContext = TestCoroutineContext()

    // Set the main coroutines dispatcher for unit testing
    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesMainDispatcherRule = ViewModelScopeMainDispatcherRule(testContext)

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // We initialise the repository with no words
        wordsRepository = FakeRepository()

        // Create class under test
        editorViewModel = EditorViewModel(wordsRepository)
    }

    @Test
    fun saveNewWord_newWordAdded() = runBlocking {
        // GIVEN - new word
        val newWord = "newWord"
        (editorViewModel).apply {
            word.value = newWord
        }

        // WHEN - the new word is saved
        editorViewModel.saveWord()

        // Execute pending coroutines actions
        testContext.triggerActions()

        // Getting the newly added word from the repository
        val newAddedWord = wordsRepository.wordsServiceData.values.first()

        // THEN - new word is added in the repository
        assertThat(newAddedWord.word).isEqualTo(newWord)

        // Then success message is shown
        assertLiveDataEventTriggered(editorViewModel.message, R.string.word_saved)
    }

    @Test
    fun saveNewWord_emptyWord_error() {
        // GIVEN - word is empty
        (editorViewModel).apply {
            word.value = "" // Make sure the word is empty
        }

        // WHEN - the word is saved
        editorViewModel.saveWord()

        // THEN - empty word error is shown
        assertLiveDataEventTriggered(editorViewModel.message, R.string.error_empty_word)
    }

    @Test
    fun saveNewWord_nullWord_error() {
        // GIVEN - word is null
        (editorViewModel).apply {
            word.value = null // Make sure the word is null
        }

        // WHEN - the word is saved
        editorViewModel.saveWord()

        // THEN - empty word error is shown
        assertLiveDataEventTriggered(editorViewModel.message, R.string.error_empty_word)
    }

}