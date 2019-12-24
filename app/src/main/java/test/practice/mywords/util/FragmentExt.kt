package test.practice.mywords.util

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun <T : ViewModel> Fragment.obtainViewModel(modelClass: Class<T>, factory: ViewModelFactory): T {
    return ViewModelProvider(this, factory)[modelClass]
}

fun Fragment.closeSoftKeyboard() {
    val view = activity?.currentFocus
    view?.let {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(it.windowToken, 0)
    }
}