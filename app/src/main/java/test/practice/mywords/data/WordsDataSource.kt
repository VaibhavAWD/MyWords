package test.practice.mywords.data

interface WordsDataSource {

    suspend fun getWords(): Result<List<Word>>

    suspend fun getWord(word: String): Result<Word>

    suspend fun saveWord(word: Word)

    suspend fun deleteWord(word: String)

    suspend fun deleteAllWords()

}