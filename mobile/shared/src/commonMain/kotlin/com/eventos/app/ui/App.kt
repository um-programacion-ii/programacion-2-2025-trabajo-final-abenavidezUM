package com.eventos.app.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.eventos.app.ui.screens.LoginScreen
import com.eventos.app.ui.theme.EventosAppTheme

/**
 * Componente raíz de la aplicación
 */
@Composable
fun App() {
    EventosAppTheme {
        Navigator(LoginScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

