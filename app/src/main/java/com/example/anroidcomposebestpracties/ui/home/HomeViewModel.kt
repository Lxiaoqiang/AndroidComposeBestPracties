package com.example.anroidcomposebestpracties.ui.home

import androidx.lifecycle.viewModelScope
import com.example.anroidcomposebestpracties.base.specific.BaseEffectState
import com.example.anroidcomposebestpracties.base.specific.BaseIntent
import com.example.anroidcomposebestpracties.base.specific.BaseUIState
import com.example.anroidcomposebestpracties.base.BaseViewModelWithEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 定义UI状态.
 * 做了状态细分，在复杂业务场景下，细分会更加有价值，在demo阶段看起来意义其实不大，但是我们记住这种方式是有必要的
 */
sealed interface HomeUIState: BaseUIState {
    val isLoading: Boolean
    val searchWord: String

    data class DataState(
        override val isLoading: Boolean,
        override val searchWord: String,
        val searchResult: List<String>

    ): HomeUIState

    data class NoDataState(
        override val isLoading: Boolean,
        override val searchWord: String,
        val errorMessage: String
    ): HomeUIState

}

/**
 * 定义ViewModel状态.
 * viewmodel的状态主要为了和ui的状态隔离开，可解耦，也可以在这块做一定的数码转换等
 * 建议选择
 * 当业务简单，可以不适用，但是如果业务复杂，可以考虑使用
 */
private data class HomeViewModelState(
    val isLoading: Boolean,
    val searchWord: String,
    val errorMessage: String,
    val searchResult: List<String> = emptyList()
): BaseUIState {

    fun toUIState() = if (searchResult.isEmpty()) {
        HomeUIState.NoDataState(
            isLoading = isLoading,
            searchWord = searchWord,
            errorMessage = errorMessage
        )
    } else {
        HomeUIState.DataState(
            isLoading = isLoading,
            searchWord = searchWord,
            searchResult = searchResult
        )
    }
}

/**
 * UI事件的定义
 */
sealed interface HomeIntent: BaseIntent {

    data class SearchIntent(val searchWord: String): HomeIntent

    data class NavigateToDetail(val detail: String): HomeIntent
}

/**
 * 非常驻状态的定义
 */
sealed interface HomeEffect: BaseEffectState {
    data class ShowToast(val message: String): HomeEffect
    data class NavigateToDetail(val detail: String): HomeEffect
}


class HomeViewModel: BaseViewModelWithEffect<HomeUIState, HomeEffect>() {

    private val _uiEffect = MutableSharedFlow<HomeEffect>()
    override val uiEffect: SharedFlow<HomeEffect> = _uiEffect

    private val viewModelState = MutableStateFlow(
        HomeViewModelState(
            isLoading = true,
            searchWord = "",
            errorMessage = "",
            searchResult = arrayListOf(
                "Jim",
                "Oliver",
                "Axia",
                "David",
                "Tom",
                "Jerry"
            )
        )
    )

    override val uiState: StateFlow<HomeUIState> = viewModelState.map { it.toUIState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUIState()
        )



    override fun onIntent(intent: BaseIntent) {
        when(intent) {
            is HomeIntent.SearchIntent -> {
                //search toast
                viewModelScope.launch {
                    _uiEffect.emit(HomeEffect.ShowToast("search toast"))
                }
            }
            is HomeIntent.NavigateToDetail -> {
                viewModelScope.launch {
                    _uiEffect.emit(HomeEffect.NavigateToDetail(intent.detail))
                }
            }
        }
    }
}