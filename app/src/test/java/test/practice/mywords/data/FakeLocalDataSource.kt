package test.practice.mywords.data

import test.practice.mywords.data.Result.Error
import test.practice.mywords.data.Result.Success
import test.practice.mywords.data.local.WordsLocalDataSource

class FakeLocalDataSource(
    var words: MutableList<Word>? = mutableListOf()
) : WordsLocalDataSource(null) {

    override suspend fun getWords(): Result<List<Word>> {
        words?.let { return Success(it) }
        return Error(Exception("Words not found"))
    }

    override suspend fun getWord(word: String): Result<Word> {
        words?.firstOrNull { it.word == word }?.let { return Success(it) }
        return Error(Exception("Word not found"))
    }

    override suspend fun saveWord(word: Word) {
        words?.add(word)
    }

    override suspend fun deleteWord(word: String) {
        words?.removeIf { it.word == word }
    }

    override suspend fun deleteAllWords() {
        words = mutableListOf()
    }

}