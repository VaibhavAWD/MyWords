package test.practice.mywords.util

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import test.practice.mywords.ui.words.WordsViewModel

fun View.showSnackbar(message: Int) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

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