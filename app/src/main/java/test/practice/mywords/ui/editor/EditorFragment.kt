package test.practice.mywords.ui.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_editor.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import test.practice.mywords.R
import test.practice.mywords.databinding.FragmentEditorBinding
import test.practice.mywords.util.*

class EditorFragment : Fragment(), KodeinAware {

    override val kodein by kodein()
    private val factory: ViewModelFactory by instance()

    private lateinit var viewModel: EditorViewModel
    private lateinit var binding: FragmentEditorBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_editor, container, false)

        viewModel = obtainViewModel(EditorViewModel::class.java, factory)

        binding = FragmentEditorBinding.bind(view).apply {
            viewmodel = this@EditorFragment.viewModel
            lifecycleOwner = this@EditorFragment.viewLifecycleOwner
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupEvents()
        setupNavigation()
    }

    private fun setupEvents() {
        binding.viewmodel?.message?.observe(viewLifecycleOwner, EventObserver { message ->
            if (message == R.string.word_saved) {
                fragment_editor?.showToast(message)
            } else {
                fragment_editor?.showSnackbar(message)
            }
        })

        binding.viewmodel?.closeSoftKeyboard?.observe(viewLifecycleOwner, EventObserver {
            closeSoftKeyboard()
        })

        input_word?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.viewmodel?.saveWord()
                true
            } else {
                false
            }
        }
    }

    private fun setupNavigation() {
        binding.viewmodel?.newWordAddedEvent?.observe(viewLifecycleOwner, EventObserver {
            navigateToWordsFragment()
        })
    }

    private fun navigateToWordsFragment() {
        val action = EditorFragmentDirections.actionWordAdded()
        findNavController().navigate(action)
    }

}
