package github.io.wottrich.myapplication.udfsample.initial.presentation.viewmodel

import androidx.lifecycle.*
import github.io.wottrich.myapplication.livedata.SingleLiveEvent
import github.io.wottrich.myapplication.udfsample.initial.domain.ui.SaveUserUiEffects
import github.io.wottrich.myapplication.udfsample.initial.domain.ui.SaveUserUiState
import github.io.wottrich.myapplication.udfsample.initial.domain.usecases.SaveUserUseCase
import kotlinx.coroutines.launch

class SaveUserViewModel(
    private val getSaveUserUseCase: SaveUserUseCase
) : ViewModel() {

//    private val pendingActions = MutableSharedFlow<SaveUserUiActions>()

    private val _uiEffects = SingleLiveEvent<SaveUserUiEffects>()
    val uiEffects: LiveData<SaveUserUiEffects> = _uiEffects

    private val _uiState = MutableLiveData<SaveUserUiState>(SaveUserUiState.Initial)
    val uiState: LiveData<SaveUserUiState> = _uiState

//    init {
//        viewModelScope.launch {
//            pendingActions.collect {
//                when (it) {
//                    InitialUiActions.ConfirmName -> saveUser()
//                }
//            }
//        }
//    }

    fun onTextChanged(text: String) {
        handleOnTextChangedState(text)
    }

    private fun handleOnTextChangedState(typedText: String) {
        val state = uiState.value ?: return
        val nextState = state.copy(
            isConfirmButtonEnabled = typedText.isNotEmpty(),
            textFieldState = state.textFieldState.copy(
                text = typedText,
                errorMessage = getErrorMessageIfNeeded(typedText)
            )
        )
        _uiState.postValue(nextState)
    }

    fun onConfirmButtonClicked() {
        viewModelScope.launch {
            uiState.value?.textFieldState?.text?.let { userName ->
                if (hasMinimalTypedLength(userName)) {
                    handleRequestSaveUserState()
                    saveUser()
                } else {
                    _uiEffects.postValue(
                        SaveUserUiEffects.SnackBarError(
                            MINIMAL_TYPED_LENGTH_ERROR_MESSAGE,
                            Throwable()
                        )
                    )
                }
            }
        }
    }

    private fun handleRequestSaveUserState() {
        val state = uiState.value ?: return
        val nextState = state.copy(
            isLoading = true,
            isConfirmButtonEnabled = false,
            textFieldState = state.textFieldState.copy(isEnabled = false)
        )
        _uiState.postValue(nextState)
    }

    private fun saveUser() {
        viewModelScope.launch {
            val typedText = uiState.value?.textFieldState?.text
            typedText?.let { userName ->
                getSaveUserUseCase(userName)
                handleSuccessSaveUserState(userName)
                handleSuccessSaveUserEffect()
            }
        }
    }

    private fun handleSuccessSaveUserState(userName: String) {
        val state = uiState.value ?: return
        val nextState = state.copy(
            isLoading = false,
            isConfirmButtonEnabled = userName.isNotEmpty(),
            textFieldState = state.textFieldState.copy(isEnabled = true)
        )
        _uiState.postValue(nextState)
    }

    private fun handleSuccessSaveUserEffect() {
        _uiEffects.postValue(SaveUserUiEffects.NextScreen)
    }

//    private fun InitialUiState?.getCurrentState(typedText: String): InitialUiState? {
//        val isLoading = this?.isLoading == true
//        val isConfirmButtonEnabled = typedText.isNotEmpty() && !isLoading
//        // It's important use uiState to not lost other values
//        return this?.copy(
//            isConfirmButtonEnabled = isConfirmButtonEnabled,
//            textFieldState = TextFieldState(
//                text = typedText,
//                errorMessage = getErrorMessageIfNeeded(typedText),
//                isEnabled = !isLoading
//            )
//        )
//    }

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