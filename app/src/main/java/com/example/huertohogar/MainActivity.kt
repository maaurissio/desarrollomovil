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
 * MainActivity es el punto de entrada de la aplicación.
 * Configura la vista de contenido principal, que es el composable AppNavigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // --- CORRECCIÓN: Se asegura de que se llame al tema correcto 'TiendaAppTheme' ---
            TiendaAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // AppNavigation es el composable que maneja toda la navegación de las pantallas.
                    AppNavigation()
                }
            }
        }
    }
}
