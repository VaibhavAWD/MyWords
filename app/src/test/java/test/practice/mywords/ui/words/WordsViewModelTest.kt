@file:Suppress("DEPRECATION")

package test.practice.mywords.ui.words

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import test.practice.mywords.*
import test.practice.mywords.data.FakeRepository
import test.practice.mywords.data.Word

/**
 * Unit tests for the implementation of [WordsViewModel].
 */
class WordsViewModelTest {

    // SUT
    private lateinit var wordsViewModel: WordsViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var wordsRepository: FakeRepository

    // A CoroutineContext that can be controlled from tests
    private val testContext = TestCoroutineContext()

    // Set the main coroutines dispatcher for unit testing
    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesMainDispatcherRule =
        ViewModelScopeMainDispatcherRule(testContext)

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        // we initialize repository with 2 words
        val word1 = Word("word1")
        val word2 = Word("word2")
        wordsRepository = FakeRepository()
        wordsRepository.addWords(word1, word2)
        wordsViewModel = WordsViewModel(wordsRepository)
    }

    @Test
    fun loadWordFromRepository_loadingTogglesAndDataLoaded() = runBlocking {
        // GIVEN
        // WHEN - initialized WordsViewModel with initialized words
        wordsViewModel.loadWords(true)

        // THEN -
        // Progress indicator is shown
        assertThat(LiveDataTestUtil.getValue(wordsViewModel.dataLoading)).isTrue()

        // Execute pending coroutines actions
        testContext.triggerActions()

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(wordsViewModel.dataLoading)).isFalse()

        // And data loaded correctly
        assertThat(LiveDataTestUtil.getValue(wordsViewModel.words)).hasSize(2)
    }

    @Test
    fun loadWords_error() {
        // GIVEN  - repository returns error
        wordsRepository.setReturnError(true)

        // WHEN - load words
        wordsViewModel.loadWords(true)

        // THEN
        // Progress is shown
        assertThat(LiveDataTestUtil.getValue(wordsViewModel.dataLoading)).isTrue()

        // Execute pending coroutines actions
        testContext.triggerActions()

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(wordsViewModel.dataLoading)).isFalse()

        // And data is empty
        assertThat(LiveDataTestUtil.getValue(wordsViewModel.words)).isEmpty()

        // And snackbar updated
        assertLiveDataEventTriggered(wordsViewModel.snackbarMessage, R.string.error_loading_words)
    }

    @Test
    fun clickOnFab_showsAddWordUi() {
        // WHEN - adding a new word
        wordsViewModel.addNewWord()

        // THEN - the event is triggered
        assertLiveDataEventTriggered(wordsViewModel.newWordEvent, Unit)
    }

    @Test
    fun clickOnOpenWord_setsEvent() {
        // WHEN - opening a word
        val word = "some_word"
        wordsViewModel.openWord(word)

        // THEN - the event is triggered
        assertLiveDataEventTriggered(wordsViewModel.openWordEvent, word)
    }

    @Test
    fun deleteWord_deletesWords() {
        // WHEN - all words are deleted
        wordsViewModel.deleteAllWords()

        // THEN - the words list is empty
        // loading words
        wordsViewModel.loadWords(true)

        // execute pending coroutines actions
        testContext.triggerActions()

        // verify that list is empty
        assertThat(LiveDataTestUtil.getValue(wordsViewModel.words)).isEmpty()

        // verify that snackbar message is shown
        assertLiveDataEventTriggered(wordsViewModel.snackbarMessage, R.string.all_words_deleted)
    }

}