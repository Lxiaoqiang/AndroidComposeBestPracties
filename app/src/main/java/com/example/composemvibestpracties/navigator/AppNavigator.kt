package com.example.composemvibestpracties.navigator

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import kotlinx.serialization.Serializable

interface AppNavigator {

    fun navigateTo(screen: Screen)

    fun navigateBack()
}

class AppNavigatorImpl(
    private val navController: NavController
): AppNavigator {
    override fun navigateTo(screen: Screen) {
        navController.navigate(screen)
    }

    override fun navigateBack() {
        navController.popBackStack()
    }
}

val LocalAppNavigator = staticCompositionLocalOf<AppNavigator> {
    error("AppNavigator not provided")
}

sealed interface Screen {

    @Serializable
    object Home: Screen

    @Serializable
    object Widget: Screen

    @Serializable
    object ValidateCodePage: Screen
}