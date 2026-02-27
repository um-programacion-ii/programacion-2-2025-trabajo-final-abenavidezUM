package com.eventos.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.eventos.app.data.models.Venta
import com.eventos.app.ui.viewmodel.SalesHistoryScreenModel
import com.eventos.app.ui.viewmodel.SalesHistoryUiState

/**
 * Pantalla de historial de ventas
 */
class SalesHistoryScreen : Screen {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { SalesHistoryScreenModel() }
        val uiState by screenModel.uiState.collectAsState()
        
        var selectedVenta by remember { mutableStateOf<Venta?>(null) }
        
        // Dialog de detalle
        selectedVenta?.let { venta ->
            AlertDialog(
                onDismissRequest = { selectedVenta = null },
                title = { Text("Detalle de Compra") },
                text = {
                    Column {
                        Text("ID: ${venta.id}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fecha: ${venta.fechaVenta}")
                        Text("Resultado: ${if (venta.resultado == true) "✅ Exitosa" else "❌ Fallida"}")
                        Text("Total: $ ${venta.precioTotal.toInt()}")
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Asientos:", fontWeight = FontWeight.Bold)
                        venta.asientos.forEach { asiento ->
                            Text(
                                "• Fila ${asiento.fila + 1}, Col ${asiento.columna + 1} - ${asiento.nombrePersona} ${asiento.apellidoPersona}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedVenta = null }) {
                        Text("Cerrar")
                    }
                }
            )
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Mis Compras") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Filled.ArrowBack, "Volver")
                        }
                    },
                    actions = {
                        IconButton(onClick = { screenModel.loadVentas() }) {
                            Icon(Icons.Default.Refresh, "Actualizar")
                        }
                    }
                )
            }
        ) { paddingValues ->
            when (val state = uiState) {
                is SalesHistoryUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is SalesHistoryUiState.Empty -> {
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
                                text = "No tienes compras registradas",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Tus compras aparecerán aquí",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                is SalesHistoryUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.ventas) { venta ->
                            SaleCard(
                                venta = venta,
                                onClick = { selectedVenta = venta }
                            )
                        }
                    }
                }
                
                is SalesHistoryUiState.Error -> {
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
                                text = "Error al cargar historial",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(text = state.message)
                            Button(onClick = { screenModel.loadVentas() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun SaleCard(
        venta: Venta,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Compra #${venta.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Surface(
                        color = if (venta.resultado == true) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (venta.resultado == true) "✅ Exitosa" else "❌ Fallida",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (venta.resultado == true) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "📅 ${venta.fechaVenta}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "💺 ${venta.asientos.size} asiento(s)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "$ ${venta.precioTotal.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Toca para ver detalles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

