package test.practice.mywords.data.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import test.practice.mywords.data.Result
import test.practice.mywords.data.Result.Error
import test.practice.mywords.data.Result.Success
import test.practice.mywords.data.Word
import test.practice.mywords.data.WordsDataSource

class WordsLocalDataSource(
    private val wordsDao: WordsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WordsDataSource {

    override suspend fun getWords(): Result<List<Word>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(wordsDao.getAllWords())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getWord(word: String): Result<Word> = withContext(ioDispatcher) {
        return@withContext try {
            Success(wordsDao.getWord(word))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun saveWord(word: Word) = withContext(ioDispatcher) {
        wordsDao.insertWord(word)
    }

    override suspend fun deleteWord(word: String) = withContext(ioDispatcher) {
        wordsDao.deleteWord(word)
    }

    override suspend fun deleteAllWords() {
        wordsDao.deleteAllWords()
    }

}