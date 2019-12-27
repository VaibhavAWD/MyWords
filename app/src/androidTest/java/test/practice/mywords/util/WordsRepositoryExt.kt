package test.practice.mywords.util

import kotlinx.coroutines.runBlocking
import test.practice.mywords.data.Word
import test.practice.mywords.data.WordsRepository

/**
 * A blocking version of WordsRepository.saveWord tp minimize the number of times we have to
 * explicitly add <code>runBlocking { ... }</code> in our tests
 */
fun WordsRepository.saveWordBlocking(word: Word) = runBlocking {
    this@saveWordBlocking.saveWord(word)
}

fun WordsRepository.getWordsBlocking(forceUpdate: Boolean) = runBlocking {
    this@getWordsBlocking.getWords(forceUpdate)
}

fun WordsRepository.deleteAllWordsBlocking() = runBlocking {
    this@deleteAllWordsBlocking.deleteAllWords()
}