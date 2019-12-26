package test.practice.mywords.util

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers
import test.practice.mywords.data.Word
import test.practice.mywords.ui.words.WordsListAdapter

fun wordDoesNotExists(word: Word): ViewAssertion {
    return RecyclerViewItemAssertion(word)
}

class RecyclerViewItemAssertion(private val word: Word) : ViewAssertion {

    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
            throw noViewFoundException
        }

        if (view !is RecyclerView) {
            throw IllegalStateException("The asserted view is not a RecyclerView")
        }

        if (view.adapter == null) {
            throw IllegalStateException("No adapter is assigned to RecyclerView")
        }

        (view.adapter as WordsListAdapter).let {
            ViewMatchers.assertThat(it.getWords()?.contains(word), CoreMatchers.equalTo(false))
        }
    }

}