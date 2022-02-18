package github.io.wottrich.myapplication.udfsample.initial.domain.ui

import androidx.lifecycle.LiveData
import github.io.wottrich.myapplication.textfield.TextFieldState
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MutableStateLiveData<T: Any>(initialValue: T) : StateLiveData<T>(initialValue) {
    public override fun postValue(value: T) {
        super.postValue(value)
    }

    public override fun setValue(value: T) {
        super.setValue(value)
    }
}

open class StateLiveData<T : Any>(initialValue: T) : LiveData<T>(initialValue) {
    override fun getValue(): T {
        return checkNotNull(super.getValue())
    }
}

abstract class ObservableState<T : Any>(initialState: T) {
    private val _state = MutableStateLiveData<T>(initialState)
    val state: StateLiveData<T> = _state

    class StateChanger<T : Any, R>(
        private val getter: T.() -> R,
        private val setter: T.(R) -> T
    ) : ReadWriteProperty<ObservableState<T>, R> {
        override fun getValue(thisRef: ObservableState<T>, property: KProperty<*>): R {
            return thisRef.state.value.getter()
        }

        override fun setValue(thisRef: ObservableState<T>, property: KProperty<*>, value: R) {
            thisRef._state.value = thisRef.state.value.setter(value)
        }
    }
}

class InitialUiStateLiveDataImpl(initialValue: InitialUiState) : ObservableState<InitialUiState>(initialValue) {
    var isLoading: Boolean by StateChanger(InitialUiState::isLoading) { copy(isLoading = it) }
    var isConfirmButtonEnabled: Boolean by StateChanger(InitialUiState::isConfirmButtonEnabled) {
        copy(isConfirmButtonEnabled = it)
    }
    var textFieldState: TextFieldState by StateChanger(InitialUiState::textFieldState) {
        copy(textFieldState = it)
    }
}

data class InitialUiState(
    val isConfirmButtonEnabled: Boolean,
    val isLoading: Boolean,
    val textFieldState: TextFieldState
) {
    companion object {
        val Initial = InitialUiState(
            isConfirmButtonEnabled = false,
            isLoading = false,
            textFieldState = TextFieldState.Initial
        )
    }
}