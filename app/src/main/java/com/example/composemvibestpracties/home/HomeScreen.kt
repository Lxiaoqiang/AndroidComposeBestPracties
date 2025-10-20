package com.example.composemvibestpracties.home

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.composemvibestpracties.Widget


typealias onHomeViewModelEvent = (HomeViewModelUIEvent) -> Unit

@Composable
fun HomeScreen(
    navigationController: NavController,
    viewModel: HomeViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        //we handle the all action-state from viewmodel to ui layer
        viewModel.uiActionState.collect {
            when (it) {
                is HomeViewModelActionState.Toast -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
                is HomeViewModelActionState.NavigateLogin -> {
                    //navigate to other page
                }
                is HomeViewModelActionState.GotoWidgetPage -> {
                    navigationController.navigate(Widget)
                }
            }
        }
    }

    HomePage(uiState) {
        viewModel.onEvent(it)
    }
}


@Composable
fun HomePage(
    uiState: HomeUIState,
    onHomeViewModelEvent: onHomeViewModelEvent
) {
    when (uiState) {
        is HomeUIState.HasResultState -> {
            HasResultPage(uiState, onHomeViewModelEvent)
        }
        is HomeUIState.NoResultState -> {
            NoResultPage(uiState, onHomeViewModelEvent)
        }
    }
}

@Composable
fun NoResultPage(uiState: HomeUIState.NoResultState, onHomeViewModelEvent: onHomeViewModelEvent) {
    Column {
        Button(
            //tigger the refresh event to viewmodel
            onClick = { onHomeViewModelEvent(HomeViewModelUIEvent.Refresh) }
        ) {
            Text("Refresh")
        }
        Button(
            //tigger the login event to viewmodel
            onClick = { onHomeViewModelEvent(HomeViewModelUIEvent.Login("username", "password")) }
        ) {
            Text("Login")
        }
        Button(
            onClick = {
                onHomeViewModelEvent(HomeViewModelUIEvent.GotoWidgetPage)
            }
        ) {
            Text("Goto WidgetPage")
        }
    }
}

@Composable
fun HasResultPage(uiState: HomeUIState.HasResultState, onHomeViewModelEvent: onHomeViewModelEvent) {
    LazyColumn {
        itemsIndexed(uiState.dataList) { index, item ->
            Text(item)
        }
    }
}