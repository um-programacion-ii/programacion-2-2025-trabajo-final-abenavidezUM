package com.eventos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.eventos.app.data.session.SessionManager
import com.eventos.app.data.session.SessionState
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión
 */
class LoginScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val sessionManager = remember { SessionManager.getInstance() }
        val scope = rememberCoroutineScope()
        
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var checkingSession by remember { mutableStateOf(true) }
        var showExpiredDialog by remember { mutableStateOf(false) }
        
        // Verificar sesión activa al iniciar
        LaunchedEffect(Unit) {
            checkingSession = true
            val state = sessionManager.checkActiveSession()
            checkingSession = false
            
            when (state) {
                is SessionState.Active -> {
                    // Hay una sesión activa, navegar a PersonDataScreen
                    navigator.replace(PersonDataScreen(eventId = state.sesion.eventoId))
                }
                is SessionState.Expired -> {
                    showExpiredDialog = true
                }
                SessionState.NoSession -> {
                    // No hay sesión, mostrar pantalla de login normal
                }
            }
        }
        
        // Dialog de sesión expirada
        if (showExpiredDialog) {
            AlertDialog(
                onDismissRequest = { showExpiredDialog = false },
                title = { Text("Sesión Expirada") },
                text = { Text("Tu sesión de compra ha expirado. Por favor, inicia sesión y comienza una nueva compra.") },
                confirmButton = {
                    Button(onClick = { 
                        showExpiredDialog = false
                        scope.launch {
                            sessionManager.clearSession()
                        }
                    }) {
                        Text("Entendido")
                    }
                }
            )
        }
        
        // Mostrar loading mientras verifica sesión
        if (checkingSession) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Verificando sesión...")
                }
            }
            return
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Iniciar Sesión") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Eventos App",
                    style = MaterialTheme.typography.headlineLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { 
                        // TODO: Implementar login
                        isLoading = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Iniciar Sesión")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { /* TODO: Navegar a registro */ },
                    enabled = !isLoading
                ) {
                    Text("¿No tienes cuenta? Regístrate")
                }
            }
        }
    }
}

