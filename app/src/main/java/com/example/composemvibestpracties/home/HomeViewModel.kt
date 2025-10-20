package com.example.composemvibestpracties.home

import androidx.lifecycle.viewModelScope
import com.example.composemvibestpracties.base.BaseActionState
import com.example.composemvibestpracties.base.BaseActionStateViewModel
import com.example.composemvibestpracties.base.BaseUIEvent
import com.example.composemvibestpracties.base.BaseUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The event defined that from ui layer
 */
sealed interface HomeViewModelUIEvent: BaseUIEvent {
    object Refresh: HomeViewModelUIEvent

    data class Login(val username: String, val password: String): HomeViewModelUIEvent
    object GotoWidgetPage: HomeViewModelUIEvent
}

/**
 * The action-state defined that from viewmodel to ui layer
 */
sealed interface HomeViewModelActionState: BaseActionState {
    data class Toast(val message: String): HomeViewModelActionState

    data class NavigateLogin(val userId: String): HomeViewModelActionState

    object GotoWidgetPage: HomeViewModelActionState
}

/**
 * The state for ui
 */
sealed interface HomeUIState: BaseUIState {
    val isLoading: Boolean
    val errorMessage: String
    val searchInput: String


    /**
     * The State with data
     */
    data class HasResultState(
        override val isLoading: Boolean,
        override val errorMessage: String,
        override val searchInput: String,
        val dataList: List<String>
    ) : HomeUIState

    /**
     * The State without data
     */
    data class NoResultState(
        override val isLoading: Boolean,
        override val errorMessage: String,
        override val searchInput: String
    ) : HomeUIState
}

/**
 * The state for UI
 */
private data class HomeViewModelState(
    val isLoading: Boolean,
    val errorMessage: String = "",
    val searchInput: String = "",
    val dataList: List<String> = emptyList(),
) {
    fun toUiState(): HomeUIState =
        if (dataList.isEmpty()) {
            HomeUIState.NoResultState(
                isLoading = isLoading,
                errorMessage = errorMessage,
                searchInput = searchInput,
            )
        } else {
            HomeUIState.HasResultState(
                isLoading = isLoading,
                errorMessage = errorMessage,
                searchInput = searchInput,
                dataList = dataList
            )
        }
}

class HomeViewModel: BaseActionStateViewModel<HomeUIState, HomeViewModelUIEvent, HomeViewModelActionState>() {

    //The ViewModelState for its self, align the uiState
    private val viewModelState = MutableStateFlow(
        HomeViewModelState(
            isLoading = false
        )
    )

    // UI state exposed to the UI
    override val uiState = viewModelState
        .map(HomeViewModelState::toUiState)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )


    /**
     * handle the event from ui layer
     */
    override fun onEvent(event: HomeViewModelUIEvent) {
        when(event) {
            HomeViewModelUIEvent.Refresh -> {
                refreshData()
            }
            is HomeViewModelUIEvent.Login -> {
                login(event.username, event.password)
            }
            is HomeViewModelUIEvent.GotoWidgetPage -> {
                sendActionState(HomeViewModelActionState.GotoWidgetPage)
            }
        }
    }

    private fun refreshData() {
        //execute refresh data logic
    }

    private fun login(username: String, password: String) {
        //execute login logic

        //check password logic, if invalid, send action-state to UI
        if (checkValid(username, password).not()) {
            sendActionState(HomeViewModelActionState.Toast("Invalid username or password"))
            return
        }

        //.........some login logic..........

        //when login success, navigate to other page
        sendActionState(HomeViewModelActionState.NavigateLogin("123456"))
    }

    fun checkValid(username: String, password: String): Boolean {
        //some check logic
        return true
    }
}