package com.eventos.app.ui.components

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import com.eventos.app.data.session.SessionManager
import com.eventos.app.data.session.SessionState
import com.eventos.app.ui.screens.EventListScreen
import kotlinx.coroutines.launch

/**
 * Componente que monitorea el estado de la sesión
 * y navega automáticamente si expira
 */
@Composable
fun SessionMonitor(
    navigator: Navigator,
    onExpired: () -> Unit = {}
) {
    val sessionManager = remember { SessionManager.getInstance() }
    val sessionState by sessionManager.sessionState.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Iniciar polling al montar
    DisposableEffect(Unit) {
        sessionManager.startSessionPolling()
        onDispose {
            sessionManager.stopSessionPolling()
        }
    }
    
    // Reaccionar a cambios de estado
    LaunchedEffect(sessionState) {
        when (sessionState) {
            is SessionState.Expired -> {
                onExpired()
                sessionManager.clearSession()
                navigator.replaceAll(EventListScreen())
            }
            else -> {}
        }
    }
}

