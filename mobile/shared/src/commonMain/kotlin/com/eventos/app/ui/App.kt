package com.eventos.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.eventos.app.ui.screens.LoginScreen

/**
 * Componente raíz de la aplicación
 */
@Composable
fun App() {
    MaterialTheme {
        Navigator(LoginScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}
