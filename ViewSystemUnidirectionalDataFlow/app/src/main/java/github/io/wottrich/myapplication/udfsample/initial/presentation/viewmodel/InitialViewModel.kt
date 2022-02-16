package github.io.wottrich.myapplication.udfsample.initial.presentation.viewmodel

import androidx.annotation.MainThread
import androidx.lifecycle.*
import github.io.wottrich.myapplication.udfsample.initial.domain.ui.InitialUiActions
import github.io.wottrich.myapplication.udfsample.initial.domain.ui.InitialUiEffects
import github.io.wottrich.myapplication.udfsample.initial.domain.ui.InitialUiState
import github.io.wottrich.myapplication.udfsample.initial.domain.usecases.SaveUserUseCase
import github.io.wottrich.myapplication.livedata.SingleLiveEvent
import github.io.wottrich.myapplication.textfield.TextFieldState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// Ver o nome
// Ver como ficaria nos testes
// Ver se a galera curtiu
open class UiDispatcher<Actions, Effects, State>(initialState: State?) {

    protected open val pendingActions = MutableSharedFlow<Actions>()

    private val _effects = SingleLiveEvent<Effects>()
    val effects: LiveData<Effects> = _effects

    private val _state: MutableLiveData<State> = MutableLiveData(initialState)
    val state: LiveData<State> = _state

    @MainThread
    protected open fun emitEffect(effect: Effects) {
        _effects.setValue(effect)
    }

    protected open fun postEffect(effect: Effects?) {
        _effects.postValue(effect)
    }

    @MainThread
    protected open fun emitState(state: State) {
        _state.setValue(state)
    }

    protected open fun postState(state: State?) {
        _state.postValue(state)
    }
}

class MutableUiDispatcher<Actions, Effects, State>(initialState: State? = null) : UiDispatcher<Actions, Effects, State>(initialState) {
    public override val pendingActions = super.pendingActions

    public override fun emitEffect(effect: Effects) {
        super.emitEffect(effect)
    }

    public override fun postEffect(effect: Effects?) {
        super.postEffect(effect)
    }

    public override fun emitState(state: State) {
        super.emitState(state)
    }

    public override fun postState(state: State?) {
        super.postState(state)
    }
}

private typealias MutableInitialUiDispatcher = MutableUiDispatcher<InitialUiActions, InitialUiEffects, InitialUiState>
private typealias InitialUiDispatcher = UiDispatcher<InitialUiActions, InitialUiEffects, InitialUiState>

class InitialViewModel(
    private val getSaveUserUseCase: SaveUserUseCase,
    private val _uiDispatcher: MutableInitialUiDispatcher = MutableUiDispatcher(InitialUiState.Initial)
) : ViewModel() {

    val uiDispatcher: InitialUiDispatcher = _uiDispatcher

    init {
        viewModelScope.launch {
            _uiDispatcher.pendingActions.collect {
                when (it) {
                    InitialUiActions.ConfirmName -> saveUser()
                }
            }
        }
    }

    fun onTextChanged(text: String) {
        _uiDispatcher.postState(uiDispatcher.state.value.getCurrentState(text))
    }

    fun onConfirmButtonClicked() {
        viewModelScope.launch {
            // get current typed text
            uiDispatcher.state.value?.textFieldState?.text?.let { typedText ->
                // validate minimal typed rule
                if (hasMinimalTypedLength(typedText)) {
                    // if valid post loading
                    val loadingState = uiDispatcher.state.value?.copy(isLoading = true)
                    val newState = loadingState.getCurrentState(typedText)
                    _uiDispatcher.postState(newState)
                    // and then you will emit action to queue
                    _uiDispatcher.pendingActions.emit(InitialUiActions.ConfirmName)
                } else {
                    // if invalid post a SnackBar with error
                    _uiDispatcher.postEffect(
                        InitialUiEffects.SnackBarError(
                            MINIMAL_TYPED_LENGTH_ERROR_MESSAGE,
                            Throwable() // Throwable to log in analytics or something else
                        )
                    )
                }
            }
        }
    }

    private fun saveUser() {
        viewModelScope.launch {
            val typedText = uiDispatcher.state.value?.textFieldState?.text.orEmpty()
            uiDispatcher.state.value?.name?.let { userName ->
                getSaveUserUseCase(userName)
                val loadingState = uiDispatcher.state.value?.copy(isLoading = false)
                val newState = loadingState.getCurrentState(typedText)
                _uiDispatcher.postState(newState)
                _uiDispatcher.postEffect(InitialUiEffects.NextScreen)
            }
        }
    }

    private fun InitialUiState?.getCurrentState(typedText: String): InitialUiState? {
        val isLoading = this?.isLoading == true
        val isConfirmButtonEnabled = typedText.isNotEmpty() && !isLoading
        // It's important use uiState to not lost other values
        return this?.copy(
            name = typedText,
            isConfirmButtonEnabled = isConfirmButtonEnabled,
            textFieldState = TextFieldState(
                text = typedText,
                errorMessage = getErrorMessageIfNeeded(typedText),
                isEnabled = !isLoading
            )
        )
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