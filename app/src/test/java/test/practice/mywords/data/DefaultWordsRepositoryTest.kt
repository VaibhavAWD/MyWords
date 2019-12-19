package test.practice.mywords.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before

import org.junit.Test
import test.practice.mywords.data.Result.Error
import test.practice.mywords.data.Result.Success

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
class DefaultWordsRepositoryTest {

    // SUT
    private lateinit var wordsRepository: DefaultWordsRepository

    private lateinit var wordsRemoteDataSource: FakeDataSource
    private lateinit var wordsLocalDataSource: FakeDataSource

    private val word1 = Word("word1")
    private val word2 = Word("word2")
    private val word3 = Word("word3")
    private val newWord = Word("newWord")
    private val remoteWords = listOf(word1, word2).sortedBy { it.word }
    private val localWords = listOf(word3).sortedBy { it.word }
    private val newWords = listOf(word3).sortedBy { it.word }

    @Before
    fun createRepository() {
        wordsRemoteDataSource = FakeDataSource(remoteWords.toMutableList())
        wordsLocalDataSource = FakeDataSource(localWords.toMutableList())
        wordsRepository = DefaultWordsRepository(wordsRemoteDataSource, wordsLocalDataSource)
    }

    // get words _ empty repository and uninitialized cache
    @Test
    fun getWords_emptyRepositoryAndUninitializedCache() = runBlocking {
        // GIVEN - repository is empty
        val emptySource = FakeDataSource()
        val wordsRepository = DefaultWordsRepository(emptySource, emptySource)

        // WHEN - getting all words
        // THEN - its success
        assertThat(wordsRepository.getWords() is Success).isTrue()
    }

    // get words _ repository caches after first api call
    @Test
    fun getWords_repositoryCachesAfterFirstApiCall() = runBlocking {
        // GIVEN - initially the repository loads data from remote and caches
        val initial = wordsRepository.getWords()

        // WHEN - update remote with new words data
        wordsRemoteDataSource.words = newWords.toMutableList()

        // THEN - initial and second should match because we did not force a refresh
        val second = wordsRepository.getWords()
        assertThat(second).isEqualTo(initial)
    }

    // get words _ requests all words from remote data source
    @Test
    fun getWords_requestsAllWordsFromRemoteDataSource() = runBlocking {
        // WHEN - words are requested from words repository
        val words = wordsRepository.getWords() as Success

        // THEN - words are loaded from the remote data source
        assertThat(words.data).isEqualTo(remoteWords)
    }

    // save word _ saves to cache local and remote
    @Test
    fun saveWord_savesToCacheLocalAndRemote() = runBlocking {
        // GIVEN - newWord is not in the remote or local data source or cache
        assertThat(wordsRemoteDataSource.words).doesNotContain(newWord)
        assertThat(wordsLocalDataSource.words).doesNotContain(newWord)
        assertThat((wordsRepository.getWords() as? Success)?.data).doesNotContain(newWord)

        // WHEN - a new word is saved to words repository
        wordsRepository.saveWord(newWord)

        // THEN - the remote and local sources are called and the cache is updated
        assertThat(wordsRemoteDataSource.words).contains(newWord)
        assertThat(wordsLocalDataSource.words).contains(newWord)

        val result = wordsRepository.getWords() as? Success
        assertThat(result?.data).contains(newWord)
    }

    // get words _ with dirty cache _ words are retrieved from remote
    @Test
    fun getWords_withDirtyCache_wordsAreRetrievedFromRemote() = runBlocking {
        // First call returns from REMOTE
        val words = wordsRepository.getWords()

        // Set a different list of words in REMOTE
        wordsRemoteDataSource.words = newWords.toMutableList()

        // But if words are cached, subsequent calls loads from cache
        val cachedWords = wordsRepository.getWords()
        assertThat(cachedWords).isEqualTo(words)

        // Now force remote loading
        val refreshedWords = wordsRepository.getWords(true) as Success

        // Words must be the recently updated in REMOTE
        assertThat(refreshedWords.data).isEqualTo(newWords)
    }

    // get words _ with dirty cache _ remote unavailable _ error
    @Test
    fun getWords_withDirtyCache_remoteUnavailable_error() = runBlocking {
        // GIVEN - remote data source unavailable
        wordsRemoteDataSource.words = null

        // WHEN - load words forcing remote
        val refreshedWords = wordsRepository.getWords(true)

        // THEN - result should be an error
        assertThat(refreshedWords).isInstanceOf(Error::class.java)
    }

    // get words _ with remote data source unavailable _ words are retrieved from local
    @Test
    fun getWords_withRemoteDataSourceUnavailable_wordsAreRetrievedFromLocal() = runBlocking {
        // GIVEN - remote data source unavailable
        wordsRemoteDataSource.words = null

        // WHEN - load words
        val result = wordsRepository.getWords()

        // THEN - result contains words from local data source
        assertThat((result as Success).data).isEqualTo(localWords)
    }

    // get words _ with both data sources unavailable _ returns error
    @Test
    fun getWords_withBothDataSourcesUnavailable_returnsError() = runBlocking {
        // GIVEN - both data sources unavailable
        wordsRemoteDataSource.words = null
        wordsLocalDataSource.words = null

        // WHEN - load words
        val result = wordsRepository.getWords()

        // THEN - error is returned
        assertThat(result).isInstanceOf(Error::class.java)
    }

    // get words _ refreshes local data source
    @Test
    fun getWords_refreshesLocalDataSource() = runBlocking {
        // GIVEN - initial local words
        val initialLocalWords = wordsLocalDataSource.words

        // WHEN - load words from repository, words are loaded from remote
        val newWords = (wordsRepository.getWords() as Success).data

        // THEN - following results are expected
        assertThat(newWords).isEqualTo(remoteWords)
        assertThat(newWords).isEqualTo(wordsLocalDataSource.words)
        assertThat(wordsLocalDataSource.words).isNotEqualTo(initialLocalWords)
    }

    // save word _ saves word to remote and updates cache
    @Test
    fun saveWord_savesWordToRemoteAndUpdatesCache() = runBlocking {
        // GIVEN - a word is saved
        wordsRepository.saveWord(newWord)

        // Verify it's in all the data sources
        assertThat(wordsRemoteDataSource.words).contains(newWord)
        assertThat(wordsLocalDataSource.words).contains(newWord)

        // WHEN - load words, it loads from cache and not from remote or local data sources
        wordsRemoteDataSource.deleteAllWords() // make sure they don't come from remote
        wordsLocalDataSource.deleteAllWords() // make sure they don't come from local
        val result = wordsRepository.getWords() as Success

        // THEN - result contains the new saved word
        assertThat(result.data).contains(newWord)
    }

    // get word _ repository caches after first api call
    @Test
    fun getWord_repositoryCachesAfterFirstApiCall() = runBlocking {
        // GIVEN - word is loaded, which is loaded from remote
        wordsRemoteDataSource.words = mutableListOf(word1)
        wordsRepository.getWord(word1.word)

        // WHEN - remote data source has different word
        wordsRemoteDataSource.words = mutableListOf(word2)
        val word1SecondTime = wordsRepository.getWord(word1.word) as Success
        val word2SecondTime = wordsRepository.getWord(word2.word) as Success

        // THEN - following results are expected
        assertThat(word1SecondTime.data.word).isEqualTo(word1.word)
        assertThat(word2SecondTime.data.word).isEqualTo(word2.word)
    }

    // get word _ force refresh
    @Test
    fun getWord_forceRefresh() = runBlocking {
        // GIVEN - word is loaded, which is loaded from remote
        wordsRemoteDataSource.words = mutableListOf(word1)
        wordsRepository.getWord(word1.word)

        // WHEN - remote data source has different word
        wordsRemoteDataSource.words = mutableListOf(word2)
        val word1SecondTime = wordsRepository.getWord(word1.word, true) as Success
        val word2SecondTime = wordsRepository.getWord(word2.word, true) as Success

        // THEN - following results are expected
        assertThat(word1SecondTime.data.word).isEqualTo(word1.word)
        assertThat(word2SecondTime.data.word).isEqualTo(word2.word)
    }

    // get word _ with dirty cache _ remote unavailable _ error
    @Test
    fun getWord_withDirtyCache_remoteUnavailable_error() = runBlocking {
        // GIVEN - remote data source unavailable
        wordsRemoteDataSource.words = null

        // WHEN - load word forcing remote
        val refreshedWord = wordsRepository.getWord(word1.word, true)

        // THEN - result should be an error
        assertThat(refreshedWord).isInstanceOf(Error::class.java)
    }

    // get word _ with remote data source unavailable _ words are retrieved from local
    @Test
    fun getWord_withRemoteDataSourceUnavailable_wordIsRetrievedFromLocal() = runBlocking {
        // GIVEN - remote data source unavailable
        wordsRemoteDataSource.words = null

        // WHEN - load word
        wordsLocalDataSource.words = mutableListOf(newWord) // make sure new word is present in local
        val result = wordsRepository.getWord(newWord.word) as Success

        // THEN - result contains word from local data source
        assertThat(result.data).isEqualTo(newWord)
    }

    // get word _ with both data sources unavailable _ returns error
    @Test
    fun getWord_withBothDataSourcesUnavailable_returnsError() = runBlocking {
        // GIVEN - both data sources unavailable
        wordsRemoteDataSource.words = null
        wordsLocalDataSource.words = null

        // WHEN - load word
        val result = wordsRepository.getWord("")

        // THEN - error is returned
        assertThat(result).isInstanceOf(Error::class.java)
    }

    // get word _ refreshes local data source
    @Test
    fun getWord_refreshesLocalDataSource() = runBlocking {
        // WHEN - load word from repository, word is loaded from remote
        wordsRemoteDataSource.words = mutableListOf(newWord) // make sure new word is present in remote
        val newWord = (wordsRepository.getWord(newWord.word) as Success).data

        // THEN - local data source contains
        assertThat(wordsLocalDataSource.words).contains(newWord)
    }

    // delete single word
    @Test
    fun deleteSingleWord_deleteFromCacheLocalAndRemote() = runBlocking {
        // GIVEN - a word is deleted
        val initialWords = (wordsRepository.getWords() as? Success)?.data
        // delete first word
        wordsRepository.deleteWord(word1.word)

        // WHEN - load words
        val afterDeleteWords = (wordsRepository.getWords() as? Success)?.data

        // THEN - only one word was deleted from cache, local and remote
        assertThat(afterDeleteWords?.size).isEqualTo(initialWords!!.size - 1)
        assertThat(afterDeleteWords).doesNotContain(word1)
        assertThat(wordsRemoteDataSource.words).doesNotContain(word1)
        assertThat(wordsLocalDataSource.words).doesNotContain(word1)
    }

    // delete all words
    @Test
    fun deleteAllWords_deletesFromCacheLocalAndRemote() = runBlocking {
        // GIVEN - all words are deleted
        wordsRepository.deleteAllWords()

        // WHEN - load words
        val cachedWords = (wordsRepository.getWords() as? Success)?.data

        // THEN - all words are deleted from cache, local and remote
        assertThat(cachedWords)?.isEmpty()
        assertThat(wordsRemoteDataSource.words).isEmpty()
        assertThat(wordsLocalDataSource.words).isEmpty()
    }
}