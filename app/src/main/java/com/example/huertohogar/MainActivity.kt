package com.example.huertohogar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.huertohogar.ui.navigation.AppNavigation
import com.example.huertohogar.ui.theme.TiendaAppTheme

/**
 * MainActivity is the single entry point for the application.
 * It sets up the main content view, which in this case is the AppNavigation composable.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply the custom theme defined in ui.theme.TiendaAppTheme
            TiendaAppTheme {
                // A Surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // AppNavigation is the composable that handles all screen routing.
                    AppNavigation()
                }
            }
        }
    }
}
