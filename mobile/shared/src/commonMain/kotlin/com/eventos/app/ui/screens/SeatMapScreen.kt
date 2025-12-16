package com.eventos.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.eventos.app.data.models.AsientoRequest
import com.eventos.app.data.models.EstadoAsiento
import com.eventos.app.data.models.EstadoAsientoEnum
import com.eventos.app.ui.viewmodel.SeatMapScreenModel
import com.eventos.app.ui.viewmodel.SeatMapUiState

/**
 * Pantalla de mapa de asientos
 */
data class SeatMapScreen(val eventId: Long) : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { SeatMapScreenModel(eventId) }
        val uiState by screenModel.uiState.collectAsState()
        val selectedSeats by screenModel.selectedSeats.collectAsState()
        
        // Manejar navegación tras bloqueo exitoso
        LaunchedEffect(uiState) {
            if (uiState is SeatMapUiState.BlockSuccess) {
                navigator.push(PersonDataScreen(eventId = eventId))
            }
        }
        
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
            when (val state = uiState) {
                is SeatMapUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is SeatMapUiState.Success -> {
                    SeatMapContent(
                        mapa = state.mapa,
                        selectedSeats = selectedSeats,
                        onSeatClick = { fila, columna ->
                            screenModel.toggleSeatSelection(fila, columna)
                        },
                        onConfirm = {
                            screenModel.bloquearAsientos()
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                
                is SeatMapUiState.Blocking -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Bloqueando asientos...")
                        }
                    }
                }
                
                is SeatMapUiState.BlockError -> {
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
                                text = "Error al bloquear asientos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(text = state.message)
                            Button(onClick = { screenModel.resetBlockState() }) {
                                Text("Volver a intentar")
                            }
                            TextButton(onClick = { navigator.pop() }) {
                                Text("Volver")
                            }
                        }
                    }
                }
                
                is SeatMapUiState.Error -> {
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
                                text = "Error al cargar mapa",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(text = state.message)
                            Button(onClick = { screenModel.retry() }) {
                                Text("Reintentar")
                            }
                            TextButton(onClick = { navigator.pop() }) {
                                Text("Volver")
                            }
                        }
                    }
                }
                
                SeatMapUiState.BlockSuccess -> {
                    // Navegación manejada por LaunchedEffect
                }
            }
        }
    }
    
    @Composable
    private fun SeatMapContent(
        mapa: com.eventos.app.data.models.MapaAsientos,
        selectedSeats: Set<AsientoRequest>,
        onSeatClick: (Int, Int) -> Unit,
        onConfirm: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Leyenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("Libre", Color(0xFF4CAF50))
                LegendItem("Ocupado", Color.Gray)
                LegendItem("Bloqueado", Color(0xFFFF9800))
                LegendItem("Seleccionado", Color(0xFF2196F3))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Indicador de pantalla
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PANTALLA",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mapa de asientos
            LazyColumn(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(mapa.filas) { fila ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Número de fila
                        Text(
                            text = "${fila + 1}",
                            modifier = Modifier.width(24.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                        
                        // Asientos de la fila
                        for (columna in 0 until mapa.columnas) {
                            val asiento = mapa.asientos.find { 
                                it.fila == fila && it.columna == columna 
                            }
                            val isSelected = selectedSeats.contains(AsientoRequest(fila, columna))
                            
                            SeatButton(
                                fila = fila,
                                columna = columna,
                                estado = asiento?.estado ?: EstadoAsientoEnum.LIBRE,
                                isSelected = isSelected,
                                onClick = { onSeatClick(fila, columna) }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info y botón
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Asientos seleccionados: ${selectedSeats.size} / 4",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (selectedSeats.size == 4) {
                        Text(
                            text = "Máximo alcanzado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedSeats.isNotEmpty()
                    ) {
                        Text("Confirmar y Continuar")
                    }
                }
            }
        }
    }
    
    @Composable
    private fun SeatButton(
        fila: Int,
        columna: Int,
        estado: EstadoAsientoEnum,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        val (backgroundColor, borderColor, enabled) = when {
            isSelected -> Triple(Color(0xFF2196F3), Color(0xFF1976D2), true)
            estado == EstadoAsientoEnum.LIBRE -> Triple(Color(0xFF4CAF50), Color(0xFF388E3C), true)
            estado == EstadoAsientoEnum.OCUPADO -> Triple(Color.Gray, Color.DarkGray, false)
            estado == EstadoAsientoEnum.BLOQUEADO -> Triple(Color(0xFFFF9800), Color(0xFFF57C00), false)
            else -> Triple(Color.Gray, Color.DarkGray, false)
        }
        
        Box(
            modifier = Modifier
                .padding(2.dp)
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(backgroundColor)
                .clickable(enabled = enabled) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (enabled) {
                Text(
                    text = "${columna + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    @Composable
    private fun LegendItem(label: String, color: Color) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

