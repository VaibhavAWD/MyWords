package test.practice.mywords.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_words.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import test.practice.mywords.R
import test.practice.mywords.databinding.FragmentWordsBinding
import test.practice.mywords.util.EventObserver
import test.practice.mywords.util.ViewModelFactory
import test.practice.mywords.util.obtainViewModel
import test.practice.mywords.util.showSnackbar

class WordsFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: ViewModelFactory by instance()

    private lateinit var viewModel: WordsViewModel
    private lateinit var binding: FragmentWordsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_words, container, false)

        viewModel = obtainViewModel(WordsViewModel::class.java, factory)

        val wordsListAdapter = WordsListAdapter(viewModel)

        binding = FragmentWordsBinding.bind(view).apply {
            viewmodel = this@WordsFragment.viewModel
            adapter = wordsListAdapter
            lifecycleOwner = this@WordsFragment.viewLifecycleOwner
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupMessage()
        setupNavigation()
        loadWords()
    }

    private fun setupMessage() {
        binding.viewmodel?.snackbarMessage?.observe(viewLifecycleOwner, EventObserver { message ->
            fragment_words?.showSnackbar(message)
        })
    }

    private fun setupNavigation() {
        binding.viewmodel?.newWordEvent?.observe(viewLifecycleOwner, EventObserver {
            navigateToEditorFragment()
        })

        binding.viewmodel?.openWordEvent?.observe(viewLifecycleOwner, EventObserver { word ->
            navigateToWordDetailFragment(word)
        })
    }

    private fun loadWords() {
        binding.viewmodel?.loadWords()
    }

    private fun navigateToEditorFragment() {
        val action = WordsFragmentDirections.actionAddNewWord()
        findNavController().navigate(action)
    }

    private fun navigateToWordDetailFragment(word: String) {
        // TODO: Navigate to WordDetailFragment.
    }

}
