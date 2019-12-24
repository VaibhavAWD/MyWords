@file:Suppress("DEPRECATION")

package test.practice.mywords.ui.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import test.practice.mywords.LiveDataTestUtil
import test.practice.mywords.R
import test.practice.mywords.ViewModelScopeMainDispatcherRule
import test.practice.mywords.assertLiveDataEventTriggered
import test.practice.mywords.data.FakeRepository
import test.practice.mywords.data.Word

/**
 * Unit tests for implementation of [WordDetailViewModel].
 */
class WordDetailViewModelTest {

    // SUT
    private lateinit var wordDetailViewModel: WordDetailViewModel

    // Use fake repository to be injected in the viewmodel
    private lateinit var wordsRepository: FakeRepository

    // A CoroutinesContext which can be controlled from tests
    private val testContext = TestCoroutineContext()

    // Set main coroutines dispatcher for unit testing
    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesMainDispatcherRule = ViewModelScopeMainDispatcherRule(testContext)

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val word = Word("testing")

    @Before
    fun setupViewModel() {
        // init words repository with a word
        wordsRepository = FakeRepository()
        wordsRepository.addWords(word)

        // Create class under test
        wordDetailViewModel = WordDetailViewModel(wordsRepository)
    }

    // load word _ displays word
    @Test
    fun loadWord_displaysWord() {
        // When word is loaded
        wordDetailViewModel.loadWord(word.word)

        // Then progress indicator is shown
        assertThat(LiveDataTestUtil.getValue(wordDetailViewModel.dataLoading)).isTrue()

        // Execute pending coroutines actions
        testContext.triggerActions()

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(wordDetailViewModel.dataLoading)).isFalse()

        // Then the word is loaded correctly
        assertThat(LiveDataTestUtil.getValue(wordDetailViewModel.word)).isEqualTo(word)
        assertThat(LiveDataTestUtil.getValue(wordDetailViewModel.dataAvailable)).isTrue()
    }

    // load word _ error
    @Test
    fun loadWord_error() {
        // GIVEN - repository returns error
        wordsRepository.setReturnError(true) // make sure repository returns error

        // WHEN - word is loaded
        wordDetailViewModel.loadWord(word.word)

        // Execute pending coroutines actions
        testContext.triggerActions()

        // THEN - data is not loaded
        assertThat(LiveDataTestUtil.getValue(wordDetailViewModel.word)).isEqualTo(null)
        assertThat(LiveDataTestUtil.getValue(wordDetailViewModel.dataAvailable)).isFalse()
    }

    // delete word _ deletes word
    @Test
    fun deleteWord_deletesWord() {
        // Given a word is loaded
        wordDetailViewModel.loadWord(word.word)

        // When the word is deleted
        wordDetailViewModel.deleteWord()

        // Execute pending coroutines actions
        testContext.triggerActions()

        // Then word is deleted
        val words = wordsRepository.wordsServiceData.values
        assertThat(words).doesNotContain(word)

        // Then success message is shown
        assertLiveDataEventTriggered(wordDetailViewModel.message, R.string.word_deleted)

        // Then user is redirected to home screen
        assertLiveDataEventTriggered(wordDetailViewModel.wordDeletedEvent, Unit)
    }
}