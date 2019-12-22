package test.practice.mywords.ui.words

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import test.practice.mywords.R
import test.practice.mywords.data.Word
import test.practice.mywords.databinding.ItemWordBinding

class WordViewHolder(
    private val itemWordBinding: ItemWordBinding,
    private val wordsViewModel: WordsViewModel
) : RecyclerView.ViewHolder(itemWordBinding.root) {

    private val listener = object : WordsListUserActionsListener {
        override fun onWordClicked(word: Word) {
            wordsViewModel.openWord(word.word)
        }
    }

    fun bind(word: Word) {
        itemWordBinding.word = word
        itemWordBinding.listener = listener
        itemWordBinding.executePendingBindings()
    }

    companion object {
        operator fun invoke(parent: ViewGroup, wordsViewModel: WordsViewModel): WordViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_word, parent, false)
            val binding = ItemWordBinding.bind(view)
            return WordViewHolder(binding, wordsViewModel)
        }
    }
}