package test.practice.mywords.data

import com.google.common.collect.Lists
import test.practice.mywords.data.Result
import test.practice.mywords.data.Result.Error
import test.practice.mywords.data.Result.Success
import test.practice.mywords.data.Word
import test.practice.mywords.data.remote.WordsRemoteDataSource

class FakeWordsRemoteDataSource : WordsRemoteDataSource() {

    private var wordsServiceData: LinkedHashMap<String, Word> = LinkedHashMap()

    override suspend fun getWords(): Result<List<Word>> {
        val words = Lists.newArrayList(wordsServiceData.values)
        return Success(words)
    }

    override suspend fun getWord(word: String): Result<Word> {
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
}