package test.practice.mywords.util

import android.view.View
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import test.practice.mywords.ui.words.WordsViewModel

fun View.showSnackbar(message: Int) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).run {
        addCallback(object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar?) {
                EspressoIdlingResource.increment() // Set app as busy.
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                EspressoIdlingResource.decrement() // Set app as idle.
            }
        })
        show()
    }
}

fun View.showToast(message: Int) {
    Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
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