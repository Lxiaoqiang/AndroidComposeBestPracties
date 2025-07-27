package com.example.anroidcomposebestpracties.base.specific

import kotlinx.coroutines.flow.StateFlow

/**
 * UI状态的定义
 */
interface IUIState<US: BaseUIState> {

    val uiState: StateFlow<US>
}