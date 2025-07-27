package com.example.anroidcomposebestpracties.base

import com.example.anroidcomposebestpracties.base.specific.BaseEffectState
import com.example.anroidcomposebestpracties.base.specific.BaseUIState
import com.example.anroidcomposebestpracties.base.specific.IUIEffect

/**
 * 如果有非常驻UI状态，我们需要继承这个类
 */
abstract class BaseViewModelWithEffect<US : BaseUIState, EF : BaseEffectState> : BaseViewModel<US>(), IUIEffect<EF>