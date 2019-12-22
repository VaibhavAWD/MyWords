package test.practice.mywords.data

import com.google.common.collect.Lists
import test.practice.mywords.data.Result.Error
import test.practice.mywords.data.Result.Success

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class FakeRepository: WordsRepository {

    var wordsServiceData: LinkedHashMap<String, Word> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getWords(forceUpdate: Boolean): Result<List<Word>> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        val words = Lists.newArrayList(wordsServiceData.values)
        return Success(words)
    }

    override suspend fun getWord(word: String, forceUpdate: Boolean): Result<Word> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
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

    fun addWords(vararg words: Word) {
        for (word in words) {
            wordsServiceData[word.word] = word
        }
    }
}