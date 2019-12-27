package test.practice.mywords.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import test.practice.mywords.data.Result.Error
import test.practice.mywords.data.Result.Success
import test.practice.mywords.data.Word
import test.practice.mywords.data.succeeded

/**
 * Integration test for the [WordsLocalDataSource].
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class WordsLocalDataSourceTest {

    // SUT
    private lateinit var localDataSource: WordsLocalDataSource
    private lateinit var database: WordsDatabase

    @Before
    fun setup() {
        // using an in-memory database for testing, since it does not survive killing the process
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            WordsDatabase::class.java
        ).allowMainThreadQueries().build()

        localDataSource = WordsLocalDataSource(database.wordsDao())
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveWord_retrievesWord() = runBlocking {
        // GIVEN - a word is saved
        val word = Word("testing")
        localDataSource.saveWord(word)

        // WHEN - the word is retrieved
        val result = localDataSource.getWord(word.word)

        // THEN - same word is returned
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.word, `is`(word.word))
    }

    @Test
    fun getWords_retrieveSavedWords() = runBlocking {
        // GIVEN - two words are saved
        val word1 = Word("word1")
        val word2 = Word("word2")
        localDataSource.saveWord(word1)
        localDataSource.saveWord(word2)

        // WHEN - all words are retrieved
        val result = localDataSource.getWords()

        // THEN - the result contains the expected values
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.size, `is`(2))
        assertThat(result.data[0].word, `is`(word1.word))
        assertThat(result.data[1].word, `is`(word2.word))
    }

    @Test
    fun getWord_validWord_retrievesWord() = runBlocking {
        // GIVEN - two words are saved
        val word1 = Word("word1")
        val word2 = Word("word2")
        localDataSource.saveWord(word1)
        localDataSource.saveWord(word2)

        // WHEN - a word is retrieved
        val result = localDataSource.getWord(word1.word)

        // THEN - the result contains the expected values
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.word, `is`(word1.word))
    }

    @Test
    fun getWord_invalidWord_error() = runBlocking {
        // GIVEN - two words are saved
        val word1 = Word("word1")
        val word2 = Word("word2")
        localDataSource.saveWord(word1)
        localDataSource.saveWord(word2)

        // WHEN - a invalid word is retrieved
        val result = localDataSource.getWord("invalid_word")

        // THEN - error is returned
        assertThat(result, `is`(instanceOf(Error::class.java)))
    }

    @Test
    fun getWord_emptyWord_error() = runBlocking {
        // GIVEN - two words are saved
        val word1 = Word("word1")
        val word2 = Word("word2")
        localDataSource.saveWord(word1)
        localDataSource.saveWord(word2)

        // WHEN - a invalid word is retrieved
        val result = localDataSource.getWord("")

        // THEN - error is returned
        assertThat(result, `is`(instanceOf(Error::class.java)))
    }

    @Test
    fun deleteWord_emptyListRetrieved() = runBlocking {
        // GIVEN - a word is saved
        val word = Word("testing")
        localDataSource.saveWord(word)

        // WHEN - a word is deleted
        localDataSource.deleteWord(word.word)

        // THEN - empty list of words is retrieved
        val result = localDataSource.getWords()
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.isEmpty(), `is`(true))
    }

    @Test
    fun deleteAllWords_emptyListRetrieved() = runBlocking {
        // GIVEN - two words are saved
        val word1 = Word("word1")
        val word2 = Word("word2")
        localDataSource.saveWord(word1)
        localDataSource.saveWord(word2)

        // WHEN - all words are deleted
        localDataSource.deleteAllWords()

        // THEN - empty list of words is retrieved
        val result = localDataSource.getWords()
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.isEmpty(), `is`(true))
    }
}