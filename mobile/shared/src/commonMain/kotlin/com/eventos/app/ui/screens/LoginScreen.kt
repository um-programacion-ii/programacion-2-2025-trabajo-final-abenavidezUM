package com.eventos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.core.model.rememberScreenModel
import com.eventos.app.ui.viewmodel.LoginScreenModel
import com.eventos.app.ui.viewmodel.LoginUiState

/**
 * Pantalla de inicio de sesión
 */
class LoginScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { LoginScreenModel() }
        val uiState by screenModel.uiState.collectAsState()
        
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var usernameError by remember { mutableStateOf<String?>(null) }
        var passwordError by remember { mutableStateOf<String?>(null) }
        
        // Observar cambios de estado
        LaunchedEffect(uiState) {
            when (val state = uiState) {
                is LoginUiState.Success -> {
                    // Navegar a lista de eventos
                    navigator.replace(EventListScreen())
                }
                is LoginUiState.Error -> {
                    // El error se muestra en el Snackbar
                }
                else -> {}
            }
        }
        
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Mostrar errores en Snackbar
        LaunchedEffect(uiState) {
            if (uiState is LoginUiState.Error) {
                snackbarHostState.showSnackbar(
                    message = (uiState as LoginUiState.Error).message,
                    duration = SnackbarDuration.Short
                )
                screenModel.resetState()
            }
        }
        
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                // Logo/Título
                Text(
                    text = "Eventos App",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Gestión de Eventos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Campo Usuario
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        usernameError = null
                    },
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is LoginUiState.Loading,
                    singleLine = true,
                    isError = usernameError != null,
                    supportingText = {
                        usernameError?.let { Text(it) }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        passwordError = null
                    },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is LoginUiState.Loading,
                    visualTransformation = if (passwordVisible) 
                        VisualTransformation.None 
                    else 
                        PasswordVisualTransformation(),
                    singleLine = true,
                    isError = passwordError != null,
                    supportingText = {
                        passwordError?.let { Text(it) }
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) 
                                    Icons.Default.Visibility 
                                else 
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) 
                                    "Ocultar contraseña" 
                                else 
                                    "Mostrar contraseña"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botón Login
                Button(
                    onClick = {
                        // Validar campos
                        var hasError = false
                        
                        if (username.isBlank()) {
                            usernameError = "El usuario es requerido"
                            hasError = true
                        }
                        
                        if (password.isBlank()) {
                            passwordError = "La contraseña es requerida"
                            hasError = true
                        } else if (password.length < 4) {
                            passwordError = "La contraseña debe tener al menos 4 caracteres"
                            hasError = true
                        }
                        
                        if (!hasError) {
                            screenModel.login(username, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is LoginUiState.Loading
                ) {
                    if (uiState is LoginUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Iniciar Sesión")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón Registro
                TextButton(
                    onClick = { 
                        // TODO: Navegar a pantalla de registro
                    },
                    enabled = uiState !is LoginUiState.Loading
                ) {
                    Text("¿No tienes cuenta? Regístrate")
                }
            }
        }
    }
}
