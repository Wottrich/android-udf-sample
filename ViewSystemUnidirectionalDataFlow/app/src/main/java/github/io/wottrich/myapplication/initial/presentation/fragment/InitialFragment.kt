package github.io.wottrich.myapplication.initial.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import github.io.wottrich.myapplication.databinding.FragmentInitialBinding
import github.io.wottrich.myapplication.initial.domain.ui.InitialUiEffects
import github.io.wottrich.myapplication.initial.domain.usecases.SaveUserUseCase
import github.io.wottrich.myapplication.initial.presentation.viewmodel.InitialViewModel
import github.io.wottrich.myapplication.textfield.TextFieldState

class InitialFragment : Fragment() {


    private val viewModel: InitialViewModel by viewModels {
        return@viewModels InitialViewModel.factory(SaveUserUseCase())
    }
    private var binding: FragmentInitialBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInitialBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEffects()
        observeStates()
        observeTextInputChanges()
        setupButtonAction()
    }

    private fun observeEffects() {
        viewModel.uiEffects.observe(viewLifecycleOwner) { effect ->
            when (effect) {
                InitialUiEffects.NextScreen -> binding?.root?.let { rootView ->
                    Snackbar.make(
                        rootView,
                        "Next screen",
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show() //TODO Navigation to next fragment
                }
                is InitialUiEffects.SnackBarError -> binding?.root?.let { rootView ->
                    Snackbar.make(
                        rootView,
                        effect.friendlyMessage,
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                }
                is InitialUiEffects.SnackBarSuccess -> binding?.root?.let { rootView ->
                    Snackbar.make(
                        rootView,
                        effect.message,
                        BaseTransientBottomBar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observeStates() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            handleButtonState(state.isConfirmButtonEnabled)
            handleTextFieldState(state.textFieldState)
            handleLoadingState(state.isLoading)
        }
    }

    private fun handleButtonState(buttonConfirmEnabled: Boolean) {
        binding?.confirmButton?.isEnabled = buttonConfirmEnabled
    }

    private fun handleTextFieldState(textFieldState: TextFieldState) {
        binding?.textInputLayout?.apply {
            isErrorEnabled = textFieldState.errorMessage != null
            error = textFieldState.errorMessage
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding?.loading?.isVisible = isLoading
    }

    private fun observeTextInputChanges() {
        binding?.textInput?.doOnTextChanged { text, _, _, _ ->
            viewModel.onTextChanged(text.toString())
        }
    }

    private fun setupButtonAction() {
        binding?.confirmButton?.setOnClickListener {
            viewModel.onConfirmButtonClicked()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}