package github.io.wottrich.composeunidirectionaldataflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.*
import github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.presentation.ui.InitialScreen
import github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.presentation.viewmodel.InitialViewModel
import github.io.wottrich.composeunidirectionaldataflow.ui.theme.ComposeUnidirectionalDataFlowTheme
import github.io.wottrich.composeunidirectionaldataflow.udfsample.initial.domain.usecases.SaveUserUseCase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeUnidirectionalDataFlowTheme {
                val initialViewModel by viewModels<InitialViewModel> {
                    return@viewModels InitialViewModel.factory(SaveUserUseCase())
                }
                val scaffoldState = rememberScaffoldState()
                Scaffold(
                    scaffoldState = scaffoldState
                ) {
                    InitialScreen(
                        scaffoldState = scaffoldState,
                        onNavigationToNextScreen = {},
                        initialViewModel = initialViewModel
                    )
                }
            }
        }
    }
}