package test.practice.mywords.ui.words

import test.practice.mywords.data.Word

interface WordsListUserActionsListener {

    fun onWordClicked(word: Word)
}