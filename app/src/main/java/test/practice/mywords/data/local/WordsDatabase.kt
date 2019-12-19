package test.practice.mywords.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import test.practice.mywords.data.Word

@Database(
    version = 1,
    entities = [Word::class],
    exportSchema = false
)
abstract class WordsDatabase : RoomDatabase() {

    abstract fun wordsDao(): WordsDao

    companion object {
        @Volatile
        var INSTANCE: WordsDatabase? = null

        fun getInstance(context: Context): WordsDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance =
                    buildDb(
                        context
                    )
                INSTANCE = instance
                return instance
            }
        }

        private fun buildDb(context: Context): WordsDatabase {
            return Room.databaseBuilder(
                context,
                WordsDatabase::class.java,
                "wordsDatabase.db"
            ).build()
        }
    }
}