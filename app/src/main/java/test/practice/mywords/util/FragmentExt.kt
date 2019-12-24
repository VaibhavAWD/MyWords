package test.practice.mywords.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun <T : ViewModel> Fragment.obtainViewModel(modelClass: Class<T>, factory: ViewModelFactory): T {
    return ViewModelProvider(this, factory)[modelClass]
}