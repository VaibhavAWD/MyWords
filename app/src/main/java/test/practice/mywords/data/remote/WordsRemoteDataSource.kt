package test.practice.mywords.data.remote

import com.google.common.collect.Lists
import kotlinx.coroutines.delay
import test.practice.mywords.data.Result
import test.practice.mywords.data.Result.Error
import test.practice.mywords.data.Result.Success
import test.practice.mywords.data.Word
import test.practice.mywords.data.WordsDataSource

/**
 * Implementation of the data source that adds a latency simulating network.
 */
class WordsRemoteDataSource : WordsDataSource {

    companion object {
        private const val SERVICE_LATENCY_IN_MILLIS = 2000L
    }

    private var wordsServiceData = LinkedHashMap<String, Word>(2)

    init {
        addWord("Hello")
        addWord("World!")
    }

    override suspend fun getWords(): Result<List<Word>> {
        delay(SERVICE_LATENCY_IN_MILLIS)
        val words = Lists.newArrayList(wordsServiceData.values)
        return Success(words)
    }

    override suspend fun getWord(word: String): Result<Word> {
        delay(SERVICE_LATENCY_IN_MILLIS)
        wordsServiceData[word]?.let {
            return Success(it)
        }
        return Error(Exception("Word not found"))
    }

    override suspend fun saveWord(word: Word) {
        wordsServiceData[word.word] = word
    }

    override suspend fun deleteWord(word: String) {
        wordsServiceData.remove(word)
    }

    override suspend fun deleteAllWords() {
        wordsServiceData.clear()
    }

    private fun addWord(word: String) {
        val newWord = Word(word)
        wordsServiceData[newWord.word] = newWord
    }
}
