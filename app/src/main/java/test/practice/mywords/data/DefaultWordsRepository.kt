package test.practice.mywords.data

import kotlinx.coroutines.*
import test.practice.mywords.data.Result.Error
import test.practice.mywords.data.Result.Success
import test.practice.mywords.data.local.WordsLocalDataSource
import test.practice.mywords.data.remote.WordsRemoteDataSource
import test.practice.mywords.util.EspressoIdlingResource
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class DefaultWordsRepository(
    private val wordsRemoteDataSource: WordsRemoteDataSource,
    private val wordsLocalDataSource: WordsLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): WordsRepository {

    private var cachedWords: ConcurrentMap<String, Word>? = null

    override suspend fun getWords(forceUpdate: Boolean): Result<List<Word>> {

        EspressoIdlingResource.increment() // Set app as busy.

        return withContext(ioDispatcher) {
            // respond immediately with cache if available and not dirty
            if (!forceUpdate) {
                cachedWords?.let { cachedWords ->
                    EspressoIdlingResource.decrement() // Set app as idle.
                    return@withContext Success(cachedWords.values.sortedBy { it.word })
                }
            }

            val newWords = fetchWordsFromRemoteOrLocal(forceUpdate)

            // refresh the cache with new words
            (newWords as? Success)?.let { refreshCache(it.data) }

            EspressoIdlingResource.decrement() // Set app as idle.

            cachedWords?.values?.let { words ->
                return@withContext Success(words.sortedBy { it.word })
            }

            (newWords as? Success)?.let {
                if (it.data.isEmpty()) {
                    return@withContext Success(it.data)
                }
            }

            return@withContext Error(Exception("Illegal state"))
        }
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
    private suspend fun fetchWordsFromRemoteOrLocal(forceUpdate: Boolean): Result<List<Word>> {
        // remote first
        val remoteWords = wordsRemoteDataSource.getWords()
        when (remoteWords) {
            is Error -> Timber.w("Failed to retrieve data from remote data source")
            is Success -> {
                refreshLocalDataSource(remoteWords.data)
                return remoteWords
            }
            else -> throw IllegalArgumentException()
        }

        // Don't read from local if it's forced
        if (forceUpdate) {
            return Error(Exception("Cannot force refresh: remote data source is unavailable"))
        }

        // local if remote fails
        val localWords = wordsLocalDataSource.getWords()
        if (localWords is Success) return localWords
        return Error(Exception("Failed to retrieve data from remote and local data sources"))
    }

    override suspend fun getWord(word: String, forceUpdate: Boolean): Result<Word> {

        EspressoIdlingResource.increment() // Set app as busy.

        return withContext(ioDispatcher) {
            // respond immediately with cache if available not dirty
            cachedWords?.get(word)?.let {
                EspressoIdlingResource.decrement() // Set app as idle.
                return@withContext Success(it)
            }

            val newWord = fetchWordFromRemoteOrLocal(word)

            // refresh the cache with new word
            (newWord as? Success)?.let { cacheWord(it.data) }

            EspressoIdlingResource.decrement() // Set app as idle.

            return@withContext newWord
        }
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
    private suspend fun fetchWordFromRemoteOrLocal(word: String): Result<Word> {
        // remote first
        val remoteWord = wordsRemoteDataSource.getWord(word)
        when(remoteWord) {
            is Error -> Timber.w("Failed to fetch word from remote data source")
            is Success -> {
                refreshLocalDataSource(remoteWord.data)
                return remoteWord
            }
            else -> throw IllegalArgumentException()
        }

        // local if remote fails
        val localWord = wordsLocalDataSource.getWord(word)
        if (localWord is Success) return localWord
        return Error(Exception("Failed to fetch data from remote and local data sources"))
    }

    override suspend fun saveWord(word: Word) {
        // Do in-memory cache update to keep the app UI up to date
        cacheAndPerform(word) {
            coroutineScope {
                launch { wordsRemoteDataSource.saveWord(word) }
                launch { wordsLocalDataSource.saveWord(word) }
            }
        }
    }

    override suspend fun deleteWord(word: String) {
        coroutineScope {
            launch { wordsRemoteDataSource.deleteWord(word) }
            launch { wordsLocalDataSource.deleteWord(word) }
            cachedWords?.remove(word)
        }
    }

    override suspend fun deleteAllWords() {
        coroutineScope {
            launch { wordsRemoteDataSource.deleteAllWords() }
            launch { wordsLocalDataSource.deleteAllWords() }
            cachedWords?.clear()
        }
    }

    private suspend fun refreshLocalDataSource(word: Word) {
        wordsLocalDataSource.saveWord(word)
    }

    private suspend fun refreshLocalDataSource(words: List<Word>) {
        wordsLocalDataSource.deleteAllWords()
        for (word in words) {
            wordsLocalDataSource.saveWord(word)
        }
    }

    private fun refreshCache(words: List<Word>) {
        cachedWords?.clear()
        words.sortedBy { it.word }.forEach {
            cacheAndPerform(it) {}
        }
    }

    private fun cacheWord(word: Word): Word {
        val cachedWord = Word(word.word)
        if (cachedWords == null) {
            cachedWords = ConcurrentHashMap()
        }
        cachedWords?.put(cachedWord.word, cachedWord)
        return cachedWord
    }

    private inline fun cacheAndPerform(word: Word, perform: (Word) -> Unit) {
        val cachedWord = cacheWord(word)
        perform(cachedWord)
    }

}