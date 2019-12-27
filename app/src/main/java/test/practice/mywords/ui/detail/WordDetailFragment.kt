package test.practice.mywords.ui.detail

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_word_detail.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import test.practice.mywords.R
import test.practice.mywords.databinding.FragmentWordDetailBinding
import test.practice.mywords.util.EventObserver
import test.practice.mywords.util.ViewModelFactory
import test.practice.mywords.util.obtainViewModel
import test.practice.mywords.util.showToast

class WordDetailFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: ViewModelFactory by instance()

    private lateinit var viewModel: WordDetailViewModel
    private lateinit var binding: FragmentWordDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_word_detail, container, false)

        viewModel = obtainViewModel(WordDetailViewModel::class.java, factory)

        binding = FragmentWordDetailBinding.bind(view).apply {
            viewmodel = this@WordDetailFragment.viewModel
            lifecycleOwner = this@WordDetailFragment.viewLifecycleOwner
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupEvents()
        setupNavigation()
        loadWord()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_word_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                binding.viewmodel?.deleteWord()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupEvents() {
        binding.viewmodel?.message?.observe(viewLifecycleOwner, EventObserver { message ->
            fragment_word_detail?.showToast(message)
        })

        binding.viewmodel?.dataAvailable?.observe(viewLifecycleOwner, Observer { available ->
            // show delete option only if data is available
            if (available) {
                setHasOptionsMenu(true)
            } else {
                setHasOptionsMenu(false)
            }
        })
    }

    private fun setupNavigation() {
        binding.viewmodel?.wordDeletedEvent?.observe(viewLifecycleOwner, EventObserver {
            navigateToWordsFragment()
        })
    }

    private fun loadWord() {
        binding.viewmodel?.loadWord(getWord())
    }

    private fun navigateToWordsFragment() {
        val action = WordDetailFragmentDirections.actionWordDeleted()
        findNavController().navigate(action)
    }

    private fun getWord(): String? {
        return arguments?.let {
            WordDetailFragmentArgs.fromBundle(it).word
        }
    }

}
