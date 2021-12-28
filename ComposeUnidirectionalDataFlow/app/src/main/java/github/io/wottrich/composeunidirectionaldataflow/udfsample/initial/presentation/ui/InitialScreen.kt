package github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.domain.ui.InitialUiEffects
import github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.domain.ui.InitialUiState
import github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.presentation.viewmodel.InitialViewModel
import github.io.wottrich.composeunidirectionaldataflow.textfield.TextFieldState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Composable
fun InitialScreen(
    scaffoldState: ScaffoldState,
    onNavigationToNextScreen: () -> Unit,
    initialViewModel: InitialViewModel,
) {

    val uiState by initialViewModel.uiState.collectAsState()
    val uiEffects = initialViewModel.uiEffects

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = uiEffects) {
        uiEffects.collect { effects ->

            fun showSnackbar(message: String) {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message)
                }
            }

            when (effects) {
                InitialUiEffects.NextScreen -> {
                    onNavigationToNextScreen()
                    showSnackbar("Next Screen")
                } // TODO Implement navigation here
                is InitialUiEffects.SnackBarError -> showSnackbar(effects.friendlyMessage)
                is InitialUiEffects.SnackBarSuccess -> showSnackbar(effects.message)
            }
        }
    }

    Screen(
        uiState = uiState,
        onTextChange = initialViewModel::onTextChange,
        onConfirmButtonClicked = initialViewModel::onConfirmButtonClicked
    )

}

@Composable
private fun Screen(
    uiState: InitialUiState,
    onTextChange: (String) -> Unit,
    onConfirmButtonClicked: () -> Unit
) {

    val textFieldState = uiState.textFieldState

    Column(
        modifier = Modifier.padding(all = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxTextField(textFieldState, onTextChange)

        SpaceBetweenComponents()

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onConfirmButtonClicked,
            enabled = uiState.isConfirmButtonEnabled,
        ) {
            Text(text = "Confirm")
        }

        SpaceBetweenComponents()

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxTextField(
    textFieldState: TextFieldState,
    onTextChange: (String) -> Unit
) {
    val isError = textFieldState.errorMessage != null
    Column {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = textFieldState.text.orEmpty(),
            onValueChange = onTextChange,
            isError = isError
        )
        AnimatedVisibility(visible = isError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = textFieldState.errorMessage.orEmpty(),
                style = MaterialTheme.typography.caption,
                color = Color.Red
            )
        }
    }
}

@Composable
private fun SpaceBetweenComponents() {
    Spacer(modifier = Modifier.height(8.dp))
}