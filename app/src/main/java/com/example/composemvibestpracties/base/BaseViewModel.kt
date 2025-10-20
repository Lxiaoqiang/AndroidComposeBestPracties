package com.example.composemvibestpracties.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * The base class for all ViewModels.
 *
 * define how to handle events from the UI layer.
 *
 * Standardize and restrict the externally exposed UI state.
 *
 * @param S the state for ui
 * @param E the event defined that from ui layer
 */
abstract class BaseViewModel<S: BaseUIState, E: BaseUIEvent>: ViewModel() {

    /**
     * The state for UI
     */
    abstract val uiState: StateFlow<S>

    /**
     * Events that triggers actions in the UI.
     * All of the events should be use this.
     */
    abstract fun onEvent(event: E)
}