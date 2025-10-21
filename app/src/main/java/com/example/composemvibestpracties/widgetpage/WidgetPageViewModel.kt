package com.example.composemvibestpracties.widgetpage

import com.example.composemvibestpracties.base.BaseActionState
import com.example.composemvibestpracties.base.BaseActionStateViewModel
import com.example.composemvibestpracties.base.BaseUIEvent
import com.example.composemvibestpracties.base.BaseUIState
import kotlinx.coroutines.flow.StateFlow


sealed interface WidgetPageUIEvent: BaseUIEvent {
    object ClickToNavigateValidateCodePage: WidgetPageUIEvent
}

sealed interface WidgetPageActionState: BaseActionState {

    object NavigateToValidateCodePage: WidgetPageActionState
}

sealed interface WidgetPageUIState: BaseUIState {

}


class WidgetPageViewModel: BaseActionStateViewModel<WidgetPageUIState, WidgetPageUIEvent, WidgetPageActionState>() {

    override val uiState: StateFlow<WidgetPageUIState>
        get() = TODO("Not yet implemented")

    override fun onEvent(event: WidgetPageUIEvent) {
        when(event) {
            is WidgetPageUIEvent.ClickToNavigateValidateCodePage -> {
                sendActionState(WidgetPageActionState.NavigateToValidateCodePage)
            }
        }
    }
}