package test.practice.mywords.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import test.practice.mywords.R
import test.practice.mywords.data.Word
import test.practice.mywords.data.WordsRepository
import test.practice.mywords.util.Event

class EditorViewModel(private val repository: WordsRepository) : ViewModel() {

    // Two-way DataBinding
    val word = MutableLiveData<String>()

    private val _message = MutableLiveData<Event<Int>>()
    val message: LiveData<Event<Int>> = _message

    private val _newWordAddedEvent = MutableLiveData<Event<Unit>>()
    val newWordAddedEvent: LiveData<Event<Unit>> = _newWordAddedEvent

    private val _closeSoftKeyboard = MutableLiveData<Event<Unit>>()
    val closeSoftKeyboard: LiveData<Event<Unit>> = _closeSoftKeyboard

    fun saveWord() {
        if (!hasValidData()) return
        _closeSoftKeyboard.value = Event(Unit)
        viewModelScope.launch {
            val newWord = Word(word.value!!)
            repository.saveWord(newWord)
            showMessage(R.string.word_saved)
            _newWordAddedEvent.value = Event(Unit)
        }
    }

    private fun hasValidData(): Boolean {
        var isValid = false

        val currentWord = word.value

        if (currentWord.isNullOrEmpty()) {
            showMessage(R.string.error_empty_word)
        } else {
            isValid = true
        }

        return isValid
    }

    private fun showMessage(message: Int) {
        _message.value = Event(message)
    }

}