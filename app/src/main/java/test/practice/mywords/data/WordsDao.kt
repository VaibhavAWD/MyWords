package test.practice.mywords.data

import androidx.room.*

@Dao
interface WordsDao {

    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<Word>

    @Query("SELECT * FROM words WHERE word = :word")
    suspend fun getWord(word: String): Word

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: Word)

    @Query("DELETE FROM words WHERE word = :word")
    suspend fun deleteWord(word: String)

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

}