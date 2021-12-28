package github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import github.io.wottrich.composeunidirectionaldataflow.flow.SingleShotEventBus
import github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.domain.ui.InitialUiActions
import github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.domain.ui.InitialUiEffects
import github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.domain.ui.InitialUiState
import github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.domain.usecases.SaveUserUseCase
import github.io.wottrich.composeunidirectionaldataflow.textfield.TextFieldState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InitialViewModel(
    private val getSaveUserUseCase: SaveUserUseCase
) : ViewModel() {

    private val pendingActions = MutableSharedFlow<InitialUiActions>()

//    private val _uiEffects = MutableSharedFlow<InitialUiEffects>()
//    val uiEffects: Flow<InitialUiEffects> = _uiEffects
    private val _uiEffects = SingleShotEventBus<InitialUiEffects>()
    val uiEffects: Flow<InitialUiEffects> = _uiEffects.events

    private val _uiState = MutableStateFlow<InitialUiState>(InitialUiState.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pendingActions.collect {
                when (it) {
                    InitialUiActions.ConfirmName -> saveUser()
                }
            }
        }
    }

    fun onTextChange(text: String) {
        _uiState.value = getUiState(text)
    }

    fun onConfirmButtonClicked() {
        viewModelScope.launch {
            uiState.value.textFieldState.text?.let { typedText ->
                if (hasMinimalTypedLength(typedText)) {
                    _uiState.value = uiState.value.copy(isLoading = true)
                    pendingActions.emit(InitialUiActions.ConfirmName)
                } else {
                    _uiEffects.emit(
                        InitialUiEffects.SnackBarError(
                            MINIMAL_TYPED_LENGTH_ERROR_MESSAGE,
                            Throwable() // Throwable to log in analytics or something else
                        )
                    )
                }
            }
        }
    }

    private fun getUiState(typedText: String): InitialUiState {
        val isLoading = uiState.value.isLoading
        val isConfirmButtonEnabled = typedText.isNotEmpty() && !isLoading
        return uiState.value.copy(
            name = typedText,
            isConfirmButtonEnabled = isConfirmButtonEnabled,
            textFieldState = TextFieldState(
                text = typedText,
                errorMessage = getErrorMessageIfNeeded(typedText),
                isEnabled = !isLoading
            )
        )
    }

    private fun saveUser() {
        viewModelScope.launch {
            uiState.value.name?.let { userName ->
                getSaveUserUseCase(userName)
                _uiState.value = uiState.value.copy(isLoading = false)
                _uiEffects.emit(InitialUiEffects.NextScreen)
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