package github.io.wottrich.myapplication.udfsample.initial.domain.ui

import androidx.lifecycle.LiveData
import github.io.wottrich.myapplication.textfield.TextFieldState
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MutableStateLiveData<T : Any>(initialValue: T) : StateLiveData<T>(initialValue) {
    private var state: T = initialValue

    @Synchronized
    fun getCurrentState(): T = state

    @Synchronized
    public override fun postValue(value: T) {
        state = value
        super.postValue(value)
    }

    @Synchronized
    public override fun setValue(value: T) {
        state = value
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
            synchronized(thisRef) {
                return thisRef._state.getCurrentState().getter()
            }
        }

        override fun setValue(thisRef: ObservableState<T>, property: KProperty<*>, value: R) {
            synchronized(thisRef) {
                thisRef._state.postValue(thisRef._state.getCurrentState().setter(value))
            }
        }
    }


}

class InitialUiStateLiveDataImpl(initialValue: InitialUiState) :
    ObservableState<InitialUiState>(initialValue) {
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