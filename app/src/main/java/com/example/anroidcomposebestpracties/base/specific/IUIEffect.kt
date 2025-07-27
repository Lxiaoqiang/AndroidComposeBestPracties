package com.example.anroidcomposebestpracties.base.specific

import kotlinx.coroutines.flow.SharedFlow

/**
 * 非常驻UI状态定义
 */
interface IUIEffect<EF : BaseEffectState> {


    val uiEffect: SharedFlow<EF>
}