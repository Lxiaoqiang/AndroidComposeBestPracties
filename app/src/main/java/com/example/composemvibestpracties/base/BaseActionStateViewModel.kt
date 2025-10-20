package com.example.composemvibestpracties.base

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * Provide the way that the action-state to the UI layer.
 * If you has actionState, extends this.
 *
 */
abstract class BaseActionStateViewModel<S: BaseUIState, E: BaseUIEvent, A: BaseActionState>: BaseViewModel<S, E>() {

    /**
     * Events that triggers a specific action in the UI.
     * all of the action-state trigger should be use this.
     * eg: LaunchedEffect(Unit) {
     *        viewModel.uiActionState.collect {
     *            when (it) {
     *                is ToastActionState -> {
     *                    Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
     *                }
     *            }
     *        }
     *    }
     */
    private val _uiActionState = MutableSharedFlow<BaseActionState>()
    val uiActionState: SharedFlow<BaseActionState> = _uiActionState

    /**
     * send the action-state to UI
     * @param state the action-state
     */
    fun sendActionState(state: A) {
        viewModelScope.launch {
            _uiActionState.emit(state)
        }
    }
}