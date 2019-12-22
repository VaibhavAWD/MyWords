package test.practice.mywords.ui.words

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import test.practice.mywords.data.Word

class WordsListAdapter(
    private val wordsViewModel: WordsViewModel
) : ListAdapter<Word, WordViewHolder>(WORD_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        return WordViewHolder(parent, wordsViewModel)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val currentWord = getItem(position)
        currentWord?.let {
            holder.bind(it)
        }
    }

    companion object {
        private val WORD_COMPARATOR = object : DiffUtil.ItemCallback<Word>() {
            override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
                return oldItem.word == newItem.word
            }

            override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
                return oldItem == newItem
            }
        }
    }
}