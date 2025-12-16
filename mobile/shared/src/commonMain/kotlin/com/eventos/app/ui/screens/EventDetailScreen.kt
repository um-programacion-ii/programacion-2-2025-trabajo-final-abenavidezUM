package com.eventos.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.eventos.app.data.models.EventoDetalle
import com.eventos.app.data.models.Integrante
import com.eventos.app.ui.viewmodel.EventDetailScreenModel
import com.eventos.app.ui.viewmodel.EventDetailUiState

/**
 * Pantalla de detalle de evento
 */
data class EventDetailScreen(val eventId: Long) : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { EventDetailScreenModel(eventId) }
        val uiState by screenModel.uiState.collectAsState()
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalle del Evento") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
            when (val state = uiState) {
                is EventDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is EventDetailUiState.Success -> {
                    EventDetailContent(
                        evento = state.evento,
                        onSelectSeats = {
                            navigator.push(SeatMapScreen(eventId = eventId))
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                
                is EventDetailUiState.Error -> {
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
                                text = "Error al cargar evento",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(onClick = { screenModel.retry() }) {
                                Text("Reintentar")
                            }
                            TextButton(onClick = { navigator.pop() }) {
                                Text("Volver")
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun EventDetailContent(
        evento: EventoDetalle,
        onSelectSeats: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Tipo de Evento
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = evento.tipoEvento.nombre,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Título
            Text(
                text = evento.titulo,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Card con información principal
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoRow(icon = "📅", label = "Fecha", value = evento.fecha)
                    InfoRow(icon = "📍", label = "Lugar", value = evento.lugar)
                    InfoRow(
                        icon = "💺", 
                        label = "Disponibles", 
                        value = "${evento.asientosDisponibles} de ${evento.capacidadTotal}"
                    )
                    InfoRow(
                        icon = "💰", 
                        label = "Precio", 
                        value = "$ ${evento.precio.toInt()}"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Descripción
            if (!evento.descripcion.isNullOrBlank()) {
                Text(
                    text = "Descripción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = evento.descripcion,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Integrantes
            if (evento.integrantes.isNotEmpty()) {
                Text(
                    text = "Integrantes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        evento.integrantes.forEach { integrante ->
                            IntegranteRow(integrante)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Información del mapa de asientos
            Text(
                text = "Distribución de Asientos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${evento.filas}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Filas",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${evento.columnas}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Columnas",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botón de acción
            Button(
                onClick = onSelectSeats,
                modifier = Modifier.fillMaxWidth(),
                enabled = evento.activo && evento.asientosDisponibles > 0
            ) {
                Text(
                    text = if (evento.asientosDisponibles > 0) 
                        "Seleccionar Asientos" 
                    else 
                        "Sin Disponibilidad",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (!evento.activo) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Este evento no está disponible actualmente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    @Composable
    private fun InfoRow(icon: String, label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Text(text = icon, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    @Composable
    private fun IntegranteRow(integrante: Integrante) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = integrante.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                integrante.rol?.let { rol ->
                    Text(
                        text = rol,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

