package github.io.wottrich.myapplication.udfsample.initial.domain.ui

import github.io.wottrich.myapplication.textfield.TextFieldState

data class InitialUiState(
    val name: String?,
    val isConfirmButtonEnabled: Boolean,
    val isLoading: Boolean,
    val textFieldState: TextFieldState
) {
    companion object {
        val Initial = InitialUiState(
            name = null,
            isConfirmButtonEnabled = false,
            isLoading = false,
            textFieldState = TextFieldState.Initial
        )
    }
}