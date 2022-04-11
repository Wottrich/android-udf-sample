package github.io.wottrich.myapplication.udfsample.initial.domain.ui

import github.io.wottrich.myapplication.textfield.TextFieldState

data class SaveUserUiState(
    val isConfirmButtonEnabled: Boolean,
    val isLoading: Boolean,
    val textFieldState: TextFieldState
) {
    companion object {
        val Initial = SaveUserUiState(
            isConfirmButtonEnabled = false,
            isLoading = false,
            textFieldState = TextFieldState.Initial
        )
    }
}