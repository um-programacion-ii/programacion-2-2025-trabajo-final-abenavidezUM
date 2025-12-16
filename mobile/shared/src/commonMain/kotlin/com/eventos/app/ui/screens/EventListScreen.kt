package com.eventos.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * Pantalla de listado de eventos (placeholder)
 */
class EventListScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Eventos Disponibles") },
                    actions = {
                        IconButton(onClick = { /* TODO: Logout */ }) {
                            Icon(Icons.Default.ExitToApp, "Cerrar sesión")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Lista de eventos (por implementar en ISSUE-035)")
            }
        }
    }
}
