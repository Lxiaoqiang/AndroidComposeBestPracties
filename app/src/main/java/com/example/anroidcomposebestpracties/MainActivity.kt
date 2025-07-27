package com.example.anroidcomposebestpracties

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.anroidcomposebestpracties.ui.detail.DataDetailScreen
import com.example.anroidcomposebestpracties.ui.home.HomeScreen
import com.example.anroidcomposebestpracties.ui.theme.AnroidComposeBestPractiesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navigationController = rememberNavController()

            AnroidComposeBestPractiesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navigationController,
                            startDestination = "home"
                        ) {
                            composable("home") {
                                HomeScreen(navigationController)
                            }
                            composable("detail") {
                                DataDetailScreen(navigationController)
                            }
                        }
                    }
                }
            }
        }
    }
}