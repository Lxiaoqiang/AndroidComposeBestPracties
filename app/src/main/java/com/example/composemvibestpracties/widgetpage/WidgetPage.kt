package com.example.composemvibestpracties.widgetpage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composemvibestpracties.navigator.LocalAppNavigator
import com.example.composemvibestpracties.navigator.Screen


@Composable
fun WidgetPage(
    viewModel: WidgetPageViewModel = viewModel()
) {

    val navigator = LocalAppNavigator.current

    LaunchedEffect(Unit) {
        viewModel.uiActionState.collect {
            navigator.navigateTo(Screen.ValidateCodePage)
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = {
                viewModel.onEvent(WidgetPageUIEvent.ClickToNavigateValidateCodePage)
            }
        ) {
            Text("ValidateCodePage")
        }
        Button(
            onClick = {
                viewModel.onEvent(WidgetPageUIEvent.ClickToNavigateValidateCodePage)
            }
        ) {
            Text("TabPage")
        }
    }
}

