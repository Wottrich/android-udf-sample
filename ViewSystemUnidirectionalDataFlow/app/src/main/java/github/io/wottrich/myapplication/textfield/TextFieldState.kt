package github.io.wottrich.myapplication.textfield

data class TextFieldState(
    val text: String?,
    val errorMessage: String?,
    val isEnabled: Boolean
) {

    companion object {
        val Initial = TextFieldState(text = null, errorMessage = null, isEnabled = true)
    }
}