package test.practice.mywords.ui.words

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import test.practice.mywords.R
import test.practice.mywords.data.Result.Success
import test.practice.mywords.data.Word
import test.practice.mywords.data.WordsRepository
import test.practice.mywords.util.Event

class WordsViewModel(private val repository: WordsRepository) : ViewModel() {

    private val _words = MutableLiveData<List<Word>>()
    val words: LiveData<List<Word>> = _words

    val empty: LiveData<Boolean> = Transformations.map(_words) {
        it.isEmpty()
    }

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarMessage = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarMessage

    private val _newWordEvent = MutableLiveData<Event<Unit>>()
    val newWordEvent: LiveData<Event<Unit>> = _newWordEvent

    private val _openWordEvent = MutableLiveData<Event<String>>()
    val openWordEvent: LiveData<Event<String>> = _openWordEvent

    fun loadWords(forceUpdate: Boolean = false) {
        showProgress()
        viewModelScope.launch {
            val result = repository.getWords(forceUpdate)
            if (result is Success) {
                _words.value = result.data
            } else {
                _words.value = emptyList()
                showMessage(R.string.error_loading_words)
            }
            hideProgress()
        }
    }

    fun addNewWord() {
        _newWordEvent.value = Event(Unit)
    }

    fun openWord(word: String) {
        _openWordEvent.value = Event(word)
    }

    fun deleteAllWords() = viewModelScope.launch {
        repository.deleteAllWords()
        showMessage(R.string.all_words_deleted)
        loadWords()
    }

    private fun showProgress() {
        _dataLoading.value = true
    }

    private fun hideProgress() {
        _dataLoading.value = false
    }

    private fun showMessage(message: Int) {
        _snackbarMessage.value = Event(message)
    }
}