package test.practice.mywords.util

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import test.practice.mywords.data.Word
import test.practice.mywords.ui.words.WordsListAdapter

/**
 * Custom matchers to test [RecyclerView].
 */
fun withItemCount(count: Int): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("RecyclerView with item count: $count")
        }

        override fun matchesSafely(item: RecyclerView?): Boolean {
            return item?.adapter?.itemCount == count
        }
    }
}

fun withWord(word: Word, position: Int): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("RecyclerView item: $word at position $position")
        }

        override fun matchesSafely(rv: RecyclerView?): Boolean {
            (rv?.adapter as WordsListAdapter).let { wordsListAdapter ->
                val wordAtPosition = wordsListAdapter.getWordAtPosition(position)
                return word == wordAtPosition
            }
        }

    }
}
