package com.example.anroidcomposebestpracties.ui.home

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

typealias homeUIIntent = (HomeIntent) -> Unit

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is HomeEffect.NavigateToDetail -> {
                    navController.navigate("detail")
                }
            }
        }
    }

    HomePage(uiState) { intent ->
        //所有的嵌套的事件都应该上溯到这里处理
        viewModel.onIntent(intent)
    }
}


@Composable
fun HomePage(
    uiState: HomeUIState,
    homeUIIntent: homeUIIntent
) {
    var searchText by remember { mutableStateOf(uiState.searchWord) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Input Box
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                homeUIIntent(HomeIntent.SearchIntent(it))
            },
            label = { Text("Search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is HomeUIState.DataState -> {
                HasDataComponent(uiState, homeUIIntent)
            }

            is HomeUIState.NoDataState -> {
                NoDataComponent(uiState)
            }
        }
    }
}

@Composable
fun HasDataComponent(
    uiState: HomeUIState.DataState,
    homeUIIntent: homeUIIntent
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(uiState.searchResult.size) { item ->
            Text(
                text = uiState.searchResult[item],
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        homeUIIntent(HomeIntent.NavigateToDetail("This is detail message, for test"))
                    }
            )
        }
    }
}

@Composable
fun NoDataComponent(
    uiState: HomeUIState.NoDataState
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = uiState.errorMessage,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

