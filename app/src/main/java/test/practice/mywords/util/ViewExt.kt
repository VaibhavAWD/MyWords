package test.practice.mywords.util

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import test.practice.mywords.ui.words.WordsViewModel

@BindingAdapter("android:onRefresh")
fun ScrollChildSwipeRefreshLayout.setSwipeRefreshLayoutOnRefreshListener(
    viewModel: WordsViewModel
) {
    setOnRefreshListener { viewModel.loadWords(true) }
}

@BindingAdapter("android:dividerOrientation")
fun setDividerOrientation(rv: RecyclerView, orientation: Int) {
    rv.addItemDecoration(DividerItemDecoration(rv.context, orientation))
}