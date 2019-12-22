package test.practice.mywords.ui.words

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import test.practice.mywords.data.Word

object WordsListBindings {

    @JvmStatic
    @BindingAdapter("app:words")
    fun setWords(rv: RecyclerView, words: List<Word>?) {
        with(rv.adapter as WordsListAdapter) {
            words?.let {
                submitList(it)
            }
        }
    }
}