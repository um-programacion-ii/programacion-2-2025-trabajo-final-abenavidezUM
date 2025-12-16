package com.eventos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * Pantalla de mapa de asientos
 */
data class SeatMapScreen(val eventId: Long) : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var selectedSeats by remember { mutableStateOf(0) }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Seleccionar Asientos") },
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
                    text = "Mapa de Asientos",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // TODO: Implementar mapa interactivo de asientos
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Mapa de asientos (por implementar)")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Asientos seleccionados: $selectedSeats / 4",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        navigator.push(PersonDataScreen(eventId = eventId))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedSeats > 0
                ) {
                    Text("Continuar")
                }
            }
        }
    }
}

