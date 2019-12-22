package test.practice.mywords.util

import androidx.databinding.BindingAdapter
import test.practice.mywords.ui.words.WordsViewModel

@BindingAdapter("android:onRefresh")
fun ScrollChildSwipeRefreshLayout.setSwipeRefreshLayoutOnRefreshListener(
    viewModel: WordsViewModel
) {
    setOnRefreshListener { viewModel.loadWords(true) }
}