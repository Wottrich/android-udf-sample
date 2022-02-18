package github.io.wottrich.myapplication.udfsample.initial.presentation.viewmodel

import androidx.lifecycle.*
import github.io.wottrich.myapplication.udfsample.initial.domain.ui.InitialUiEffects
import github.io.wottrich.myapplication.udfsample.initial.domain.ui.InitialUiState
import github.io.wottrich.myapplication.udfsample.initial.domain.usecases.SaveUserUseCase
import github.io.wottrich.myapplication.livedata.SingleLiveEvent
import github.io.wottrich.myapplication.textfield.TextFieldState
import github.io.wottrich.myapplication.udfsample.initial.domain.ui.InitialUiStateLiveDataImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InitialViewModel(
    private val getSaveUserUseCase: SaveUserUseCase
) : ViewModel() {

    private val _uiEffects = SingleLiveEvent<InitialUiEffects>()
    val uiEffects: LiveData<InitialUiEffects> = _uiEffects

    private val _uiState = InitialUiStateLiveDataImpl(InitialUiState.Initial)
    val uiState = _uiState.state

    fun onTextChanged(text: String) {
        changeCurrentState(text)
    }

    fun onConfirmButtonClicked() {
        saveUser()
    }

    private fun saveUser() {
        viewModelScope.launch(Dispatchers.IO) {
            // get current typed text
            uiState.value.textFieldState.text?.let { typedText ->
                // validate minimal typed rule
                if (hasMinimalTypedLength(typedText)) {
                    // if valid post loading
                    _uiState.isLoading = true
                    changeCurrentState(typedText)
                    // and then you will emit action to queue
                    uiState.value.textFieldState.text?.let { userName ->
                        getSaveUserUseCase(userName)
                        _uiState.isLoading = false
                        changeCurrentState(typedText)
                        _uiEffects.postValue(InitialUiEffects.NextScreen)
                    }
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

    private fun changeCurrentState(typedText: String) {
        val isLoading = uiState.value.isLoading
        val isConfirmButtonEnabledValue = typedText.isNotEmpty() && !isLoading
        _uiState.apply {
            isConfirmButtonEnabled = isConfirmButtonEnabledValue
            textFieldState = TextFieldState(
                text = typedText,
                errorMessage = getErrorMessageIfNeeded(typedText),
                isEnabled = !isLoading
            )
        }
    }

    private fun getErrorMessageIfNeeded(typedText: String): String? {
        val shouldShowErrorMessage = hasMinimalTypedLength(typedText) || typedText.isEmpty()
        return if (shouldShowErrorMessage) null else MINIMAL_TYPED_LENGTH_ERROR_MESSAGE
    }

    private fun hasMinimalTypedLength(typedText: String) = typedText.length >= MINIMAL_TYPED_LENGTH

    companion object {
        private const val MINIMAL_TYPED_LENGTH = 3
        private const val MINIMAL_TYPED_LENGTH_ERROR_MESSAGE =
            "Type a name with more than 3 characters"

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