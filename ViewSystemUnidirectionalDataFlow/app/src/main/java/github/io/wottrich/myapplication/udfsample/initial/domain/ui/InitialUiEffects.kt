package github.io.wottrich.myapplication.udfsample.initial.domain.ui

sealed class InitialUiEffects {
    object NextScreen : InitialUiEffects()
    data class SnackBarSuccess(val message: String) : InitialUiEffects()
    data class SnackBarError(
        val friendlyMessage: String,
        val throwable: Throwable
    ) : InitialUiEffects()
}