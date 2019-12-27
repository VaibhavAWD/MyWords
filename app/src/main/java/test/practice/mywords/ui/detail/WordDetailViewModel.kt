package test.practice.mywords.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import test.practice.mywords.R
import test.practice.mywords.data.Result.Success
import test.practice.mywords.data.Word
import test.practice.mywords.data.WordsRepository
import test.practice.mywords.util.Event

class WordDetailViewModel(private val repository: WordsRepository) : ViewModel() {

    private val _word = MutableLiveData<Word>()
    val word: LiveData<Word> = _word

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _dataAvailable = MutableLiveData<Boolean>()
    val dataAvailable: LiveData<Boolean> = _dataAvailable

    private val _message = MutableLiveData<Event<Int>>()
    val message: LiveData<Event<Int>> = _message

    private val _wordDeletedEvent = MutableLiveData<Event<Unit>>()
    val wordDeletedEvent: LiveData<Event<Unit>> = _wordDeletedEvent

    fun loadWord(word: String?) {
        if (word != null) {
            showProgress()
            viewModelScope.launch {
                val result = repository.getWord(word)
                if (result is Success) {
                    _word.value = result.data
                    _dataAvailable.value = true
                } else {
                    _word.value = null
                    _dataAvailable.value = false
                }
                hideProgress()
            }
        }
    }

    fun deleteWord() = viewModelScope.launch {
        word.value?.let {
            repository.deleteWord(it.word)
            _message.value = Event(R.string.word_deleted)
            _wordDeletedEvent.value = Event(Unit)
        }
    }

    fun onRefresh() {
        word.value?.let {
            loadWord(it.word)
        }
    }

    private fun showProgress() {
        _dataLoading.value = true
    }

    private fun hideProgress() {
        _dataLoading.value = false
    }

}