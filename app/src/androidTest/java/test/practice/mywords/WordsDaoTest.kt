package test.practice.mywords

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import test.practice.mywords.data.Word
import test.practice.mywords.data.WordsDao
import test.practice.mywords.data.WordsDatabase

@RunWith(AndroidJUnit4::class)
@SmallTest
class WordsDaoTest {

    // SUT
    private lateinit var wordsDao: WordsDao

    private lateinit var database: WordsDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            WordsDatabase::class.java
        ).allowMainThreadQueries().build()

        wordsDao = database.wordsDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertWordAndGetWord() = runBlocking {
        // GIVEN - a word is inserted
        val word = Word("testing")
        wordsDao.insertWord(word)

        // WHEN - get the word from database
        val loaded = wordsDao.getWord(word.word)

        // THEN - the loaded data contains the expected values
        assertThat<Word>(loaded, notNullValue())
        assertThat(loaded.word, `is`(word.word))
    }

    @Test
    fun insertWordReplacesOnConflict() = runBlocking {
        // GIVEN - a word is inserted
        val word = Word("testing")
        wordsDao.insertWord(word)

        // WHEN - a same word is inserted
        val newWord = Word(word.word)
        wordsDao.insertWord(newWord)

        // THEN - there is only 1 word in the database, and contains the expected values
        val loaded = wordsDao.getAllWords()
        assertThat(loaded.size, `is`(1))
        assertThat(loaded[0].word, `is`(word.word))
    }

    @Test
    fun insertWordAndGetWords() = runBlocking {
        // GIVEN - two words are inserted
        val word1 = Word("Word1")
        val word2 = Word("Word2")
        wordsDao.insertWord(word1)
        wordsDao.insertWord(word2)

        // WHEN - get all words from database
        val loaded = wordsDao.getAllWords()

        // THEN - the loaded data contains the expected values
        assertThat(loaded.size, `is`(2))
        assertThat(loaded[0].word, `is`(word1.word))
        assertThat(loaded[1].word, `is`(word2.word))
    }

    @Test
    fun deleteWordAndGettingWords() = runBlocking {
        // GIVEN - a word is inserted
        val word = Word("testing")
        wordsDao.insertWord(word)

        // WHEN - deleting a word
        wordsDao.deleteWord(word.word)

        // THEN - the list is empty
        val words = wordsDao.getAllWords()
        assertThat(words.isEmpty(), `is`(true))
    }

    @Test
    fun deleteWordsAndGettingWords() = runBlocking {
        // GIVEN - a word is inserted
        val word = Word("testing")
        wordsDao.insertWord(word)

        // WHEN - deleting a word
        wordsDao.deleteAllWords()

        // THEN - the list is empty
        val words = wordsDao.getAllWords()
        assertThat(words.isEmpty(), `is`(true))
    }
}