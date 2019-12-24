package test.practice.mywords.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import test.practice.mywords.data.WordsRepository
import test.practice.mywords.ui.editor.EditorViewModel
import test.practice.mywords.ui.words.WordsViewModel

class ViewModelFactory(
    private val repository: WordsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return with(modelClass) {
            when {
                isAssignableFrom(WordsViewModel::class.java) -> WordsViewModel(repository)
                isAssignableFrom(EditorViewModel::class.java) -> EditorViewModel(repository)
                else -> throw IllegalArgumentException("Unknown model class: $modelClass")
            }
        } as T
    }
}