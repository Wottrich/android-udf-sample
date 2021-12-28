package github.io.wottrich.myapplication.initial.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import github.io.wottrich.myapplication.initial.domain.ui.InitialUiActions
import github.io.wottrich.myapplication.initial.domain.ui.InitialUiEffects
import github.io.wottrich.myapplication.initial.domain.ui.InitialUiState
import github.io.wottrich.myapplication.initial.domain.usecases.SaveUserUseCase
import github.io.wottrich.myapplication.livedata.SingleLiveEvent
import github.io.wottrich.myapplication.textfield.TextFieldState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class InitialViewModel(
    private val getSaveUserUseCase: SaveUserUseCase
) : ViewModel() {

    private val pendingActions = MutableSharedFlow<InitialUiActions>()

    private val _uiEffects = SingleLiveEvent<InitialUiEffects>()
    val uiEffects: LiveData<InitialUiEffects> = _uiEffects

    private val _uiState = SingleLiveEvent<InitialUiState>(InitialUiState.Initial)
    val uiState: LiveData<InitialUiState> = _uiState

    init {
        viewModelScope.launch {
            pendingActions.collect {
                when (it) {
                    InitialUiActions.ConfirmName -> saveUser()
                }
            }
        }
    }

    fun onTextChanged(text: String) {
        _uiState.postValue(getUiState(text))
    }

    fun onConfirmButtonClicked() {
        viewModelScope.launch {
            // get current typed text
            uiState.value?.textFieldState?.text?.let { typedText ->
                // validate minimal typed rule
                if (hasMinimalTypedLength(typedText)) {
                    // if valid post loading
                    _uiState.postValue(uiState.value?.copy(isLoading = true))
                    // and then you will emit action to queue
                    pendingActions.emit(InitialUiActions.ConfirmName)
                } else {
                    // if invalid post a SnackBar with error
                    _uiEffects.postValue(
                        InitialUiEffects.SnackBarError(
                            MINIMAL_TYPED_LENGTH_ERROR_MESSAGE,
                            Throwable() // Throwable to log in analytics or something else
                        )
                    )
                }
            }
        }
    }

    private fun getUiState(typedText: String): InitialUiState? {
        val isLoading = uiState.value?.isLoading == true
        val isConfirmButtonEnabled = typedText.isNotEmpty() && !isLoading
        // It's important use uiState to not lost other values
        return uiState.value?.copy(
            name = typedText,
            isConfirmButtonEnabled = isConfirmButtonEnabled,
            textFieldState = TextFieldState(
                text = typedText,
                errorMessage = getErrorMessageIfNeeded(typedText),
                isEnabled = isLoading
            )
        )
    }

    private fun saveUser() {
        viewModelScope.launch {
            uiState.value?.name?.let { userName ->
                getSaveUserUseCase(userName)
                _uiState.postValue(uiState.value?.copy(isLoading = false))
                _uiEffects.postValue(InitialUiEffects.NextScreen)
            }
        }
    }

    private fun getErrorMessageIfNeeded(typedText: String): String? {
        val shouldShowErrorMessage = hasMinimalTypedLength(typedText) || typedText.isEmpty()
        return if (shouldShowErrorMessage) null else MINIMAL_TYPED_LENGTH_ERROR_MESSAGE
    }

    private fun hasMinimalTypedLength(typedText: String) = typedText.length >= MINIMAL_TYPED_LENGTH

    companion object {
        private const val MINIMAL_TYPED_LENGTH = 3
        private const val MINIMAL_TYPED_LENGTH_ERROR_MESSAGE = "Type a name with more than 3 characters"

        /* make a factory of your ViewModel or provider it using a DI */
        fun factory(saveUserUseCase: SaveUserUseCase): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return modelClass.getConstructor(
                        SaveUserUseCase::class.java
                    ).newInstance(saveUserUseCase)
                }
            }
        }
    }
}