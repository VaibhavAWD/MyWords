package test.practice.mywords.data

interface WordsRepository {

    suspend fun getWords(forceUpdate: Boolean = false): Result<List<Word>>

    suspend fun getWord(word: String, forceUpdate: Boolean = false): Result<Word>

    suspend fun saveWord(word: Word)

    suspend fun deleteWord(word: String)

    suspend fun deleteAllWords()

}