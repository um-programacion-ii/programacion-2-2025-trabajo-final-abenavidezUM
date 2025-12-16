package com.eventos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen

/**
 * Pantalla de inicio de sesión
 */
class LoginScreen : Screen {
    
    @Composable
    override fun Content() {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        
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

