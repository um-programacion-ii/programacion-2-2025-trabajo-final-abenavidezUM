package com.eventos.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.eventos.app.data.models.EventoResumen
import com.eventos.app.ui.viewmodel.EventListScreenModel
import com.eventos.app.ui.viewmodel.EventListUiState

/**
 * Pantalla de listado de eventos
 */
class EventListScreen : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { EventListScreenModel() }
        val uiState by screenModel.uiState.collectAsState()
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Eventos Disponibles") },
                    actions = {
                        IconButton(onClick = { navigator.push(SalesHistoryScreen()) }) {
                            Icon(Icons.Default.History, "Mis Compras")
                        }
                        IconButton(onClick = { screenModel.loadEventos() }) {
                            Icon(Icons.Default.Refresh, "Actualizar")
                        }
                        IconButton(onClick = { 
                            // TODO: Implementar logout
                            navigator.replaceAll(LoginScreen())
                        }) {
                            Icon(Icons.Default.ExitToApp, "Cerrar sesión")
                        }
                    }
                )
            }
        ) { paddingValues ->
            when (val state = uiState) {
                is EventListUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is EventListUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No hay eventos disponibles",
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(onClick = { screenModel.loadEventos() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                
                is EventListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.eventos) { evento ->
                            EventCard(
                                evento = evento,
                                onClick = {
                                    navigator.push(EventDetailScreen(eventId = evento.id))
                                }
                            )
                        }
                    }
                }
                
                is EventListUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Error al cargar eventos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(onClick = { screenModel.loadEventos() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun EventCard(
        evento: EventoResumen,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Título
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Descripción
                evento.descripcion?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Fecha y lugar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "📅 ${evento.fecha}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "📍 ${evento.lugar}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$ ${evento.precio.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${evento.asientosDisponibles} disponibles",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (evento.asientosDisponibles > 10) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

