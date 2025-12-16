package com.eventos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * Pantalla de carga de datos de personas
 */
data class PersonDataScreen(val eventId: Long) : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var name1 by remember { mutableStateOf("") }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Datos de Asistentes") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Ingresa los datos de los asistentes",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // TODO: Generar campos dinámicamente según asientos seleccionados
                OutlinedTextField(
                    value = name1,
                    onValueChange = { name1 = it },
                    label = { Text("Nombre Asistente 1") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        navigator.push(ConfirmationScreen(eventId = eventId))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name1.isNotBlank()
                ) {
                    Text("Continuar")
                }
            }
        }
    }
}

