package com.example.anroidcomposebestpracties.base

import androidx.lifecycle.ViewModel
import com.example.anroidcomposebestpracties.base.specific.BaseIntent
import com.example.anroidcomposebestpracties.base.specific.BaseUIState
import com.example.anroidcomposebestpracties.base.specific.IUIState

/**
 * Base class for [ViewModel] instances that follow the MVI pattern.
 */
abstract class BaseViewModel<US : BaseUIState> : ViewModel(), IUIState<US> {


    /**
     * Standardize UI intent entry points.
     */
    abstract fun onIntent(intent: BaseIntent)

}