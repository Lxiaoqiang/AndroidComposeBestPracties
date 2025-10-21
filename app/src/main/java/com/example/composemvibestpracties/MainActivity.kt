package com.example.composemvibestpracties

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.composemvibestpracties.home.HomeScreen
import com.example.composemvibestpracties.navigator.AppNavigatorImpl
import com.example.composemvibestpracties.navigator.LocalAppNavigator
import com.example.composemvibestpracties.navigator.Screen
import com.example.composemvibestpracties.ui.theme.ComposeMVIBestPractiesTheme
import com.example.composemvibestpracties.widgetpage.WidgetPage
import com.example.composemvibestpracties.widgetpage.validatecodepage.ValidateCodePage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val appNavigator = remember(navController) { AppNavigatorImpl(navController) }

            ComposeMVIBestPractiesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompositionLocalProvider(
                        LocalAppNavigator provides appNavigator
                    ) {
                        NavHost(navController, startDestination = Screen.Home, Modifier.padding(innerPadding)) {
                            composable<Screen.Home> {
                                HomeScreen()
                            }
                            composable<Screen.Widget> {
                                WidgetPage()
                            }
                            composable<Screen.ValidateCodePage> {
                                ValidateCodePage()
                            }
                        }
                    }
                }
            }
        }
    }
}