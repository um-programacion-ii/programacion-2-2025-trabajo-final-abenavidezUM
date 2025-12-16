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
import com.eventos.app.data.models.AsientoSeleccionado
import com.eventos.app.ui.viewmodel.PersonDataScreenModel
import com.eventos.app.ui.viewmodel.PersonDataUiState

/**
 * Pantalla de carga de datos de personas
 */
data class PersonDataScreen(val eventId: Long) : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { PersonDataScreenModel() }
        val uiState by screenModel.uiState.collectAsState()
        val personData by screenModel.personData.collectAsState()
        
        // Manejar navegación tras guardado exitoso
        LaunchedEffect(uiState) {
            if (uiState is PersonDataUiState.SubmitSuccess) {
                navigator.push(ConfirmationScreen(eventId = eventId))
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Datos de Asistentes") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver a selección")
                        }
                    }
                )
            }
        ) { paddingValues ->
            when (val state = uiState) {
                is PersonDataUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is PersonDataUiState.Success -> {
                    PersonDataContent(
                        asientos = state.asientos,
                        personData = personData,
                        onDataChange = { fila, columna, nombre, apellido ->
                            screenModel.updatePersonData(fila, columna, nombre, apellido)
                        },
                        onSubmit = {
                            screenModel.submitPersonData()
                        },
                        isDataComplete = screenModel.isDataComplete(),
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                
                is PersonDataUiState.Submitting -> {
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
                            Text("Guardando datos...")
                        }
                    }
                }
                
                is PersonDataUiState.SubmitError -> {
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
                                text = "Error al guardar datos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(text = state.message)
                            Button(onClick = { screenModel.resetSubmitState() }) {
                                Text("Reintentar")
                            }
                            TextButton(onClick = { navigator.pop() }) {
                                Text("Volver")
                            }
                        }
                    }
                }
                
                is PersonDataUiState.Error -> {
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
                                text = "Error al cargar sesión",
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
                
                PersonDataUiState.SubmitSuccess -> {
                    // Navegación manejada por LaunchedEffect
                }
            }
        }
    }
    
    @Composable
    private fun PersonDataContent(
        asientos: List<AsientoSeleccionado>,
        personData: Map<String, com.eventos.app.ui.viewmodel.PersonData>,
        onDataChange: (Int, Int, String, String) -> Unit,
        onSubmit: () -> Unit,
        isDataComplete: Boolean,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Ingresa los datos de los asistentes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Completa la información para cada asiento seleccionado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Campos dinámicos para cada asiento
            asientos.forEachIndexed { index, asiento ->
                val key = "${asiento.fila}-${asiento.columna}"
                val data = personData[key] ?: com.eventos.app.ui.viewmodel.PersonData("", "")
                
                PersonDataCard(
                    index = index + 1,
                    fila = asiento.fila,
                    columna = asiento.columna,
                    nombre = data.nombre,
                    apellido = data.apellido,
                    onNombreChange = { nombre ->
                        onDataChange(asiento.fila, asiento.columna, nombre, data.apellido)
                    },
                    onApellidoChange = { apellido ->
                        onDataChange(asiento.fila, asiento.columna, data.nombre, apellido)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botones
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = isDataComplete
            ) {
                Text("Continuar a Confirmación")
            }
            
            if (!isDataComplete) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Completa todos los campos para continuar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    @Composable
    private fun PersonDataCard(
        index: Int,
        fila: Int,
        columna: Int,
        nombre: String,
        apellido: String,
        onNombreChange: (String) -> Unit,
        onApellidoChange: (String) -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
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
                        text = "Asistente $index",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Fila ${fila + 1}, Asiento ${columna + 1}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nombre.isBlank()
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = apellido,
                    onValueChange = onApellidoChange,
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = apellido.isBlank()
                )
            }
        }
    }
}

