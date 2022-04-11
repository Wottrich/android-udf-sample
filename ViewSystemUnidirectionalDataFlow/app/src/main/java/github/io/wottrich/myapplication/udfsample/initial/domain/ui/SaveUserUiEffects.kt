package github.io.wottrich.myapplication.udfsample.initial.domain.ui

sealed class SaveUserUiEffects {
    object NextScreen : SaveUserUiEffects()
    data class SnackBarSuccess(val message: String) : SaveUserUiEffects()
    data class SnackBarError(
        val friendlyMessage: String,
        val throwable: Throwable
    ) : SaveUserUiEffects()
}