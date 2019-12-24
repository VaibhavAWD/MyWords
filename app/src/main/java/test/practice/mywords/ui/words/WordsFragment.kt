package test.practice.mywords.ui.words

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
        setupEvents()
        setupNavigation()
        loadWords()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_words, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                binding.viewmodel?.deleteAllWords()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupEvents() {
        binding.viewmodel?.snackbarMessage?.observe(viewLifecycleOwner, EventObserver { message ->
            fragment_words?.showSnackbar(message)
        })

        binding.viewmodel?.empty?.observe(viewLifecycleOwner, Observer { isEmpty ->
            // show delete all option only if data is available
            if (isEmpty) {
                setHasOptionsMenu(false)
            } else {
                setHasOptionsMenu(true)
            }
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
        val action = WordsFragmentDirections.actionOpenWord(word)
        findNavController().navigate(action)
    }

}
