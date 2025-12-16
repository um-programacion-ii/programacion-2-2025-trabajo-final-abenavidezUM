package com.eventos.app.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.eventos.app.data.models.ActualizarPersonasRequest
import com.eventos.app.data.models.AsientoSeleccionado
import com.eventos.app.data.models.PersonaAsientoRequest
import com.eventos.app.data.repository.SesionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonDataScreenModel : ScreenModel {
    
    private val sesionRepository = SesionRepository()
    
    private val _uiState = MutableStateFlow<PersonDataUiState>(PersonDataUiState.Loading)
    val uiState: StateFlow<PersonDataUiState> = _uiState.asStateFlow()
    
    private val _personData = MutableStateFlow<Map<String, PersonData>>(emptyMap())
    val personData: StateFlow<Map<String, PersonData>> = _personData.asStateFlow()
    
    init {
        loadSesion()
    }
    
    private fun loadSesion() {
        screenModelScope.launch {
            _uiState.value = PersonDataUiState.Loading
            
            val result = sesionRepository.getSesion()
            
            _uiState.value = if (result.isSuccess) {
                val sesion = result.getOrNull()!!
                
                // Inicializar datos de personas desde la sesión
                val initialData = sesion.asientosSeleccionados.associate { asiento ->
                    val key = "${asiento.fila}-${asiento.columna}"
                    key to PersonData(
                        nombre = asiento.persona?.nombre ?: "",
                        apellido = asiento.persona?.apellido ?: ""
                    )
                }
                _personData.value = initialData
                
                PersonDataUiState.Success(sesion.asientosSeleccionados)
            } else {
                PersonDataUiState.Error(result.exceptionOrNull()?.message ?: "Error al cargar sesión")
            }
        }
    }
    
    fun updatePersonData(fila: Int, columna: Int, nombre: String, apellido: String) {
        val key = "$fila-$columna"
        val currentData = _personData.value.toMutableMap()
        currentData[key] = PersonData(nombre, apellido)
        _personData.value = currentData
    }
    
    fun submitPersonData() {
        val currentState = _uiState.value
        if (currentState !is PersonDataUiState.Success) return
        
        screenModelScope.launch {
            _uiState.value = PersonDataUiState.Submitting
            
            val personas = currentState.asientos.map { asiento ->
                val key = "${asiento.fila}-${asiento.columna}"
                val data = _personData.value[key] ?: PersonData("", "")
                PersonaAsientoRequest(
                    fila = asiento.fila,
                    columna = asiento.columna,
                    nombre = data.nombre,
                    apellido = data.apellido
                )
            }
            
            val request = ActualizarPersonasRequest(personas)
            val result = sesionRepository.actualizarPersonas(request)
            
            _uiState.value = if (result.isSuccess) {
                PersonDataUiState.SubmitSuccess
            } else {
                PersonDataUiState.SubmitError(result.exceptionOrNull()?.message ?: "Error al guardar datos")
            }
        }
    }
    
    fun retry() {
        loadSesion()
    }
    
    fun resetSubmitState() {
        loadSesion()
    }
    
    fun isDataComplete(): Boolean {
        val currentState = _uiState.value
        if (currentState !is PersonDataUiState.Success) return false
        
        return currentState.asientos.all { asiento ->
            val key = "${asiento.fila}-${asiento.columna}"
            val data = _personData.value[key]
            !data?.nombre.isNullOrBlank() && !data?.apellido.isNullOrBlank()
        }
    }
}

data class PersonData(
    val nombre: String,
    val apellido: String
)

sealed class PersonDataUiState {
    data object Loading : PersonDataUiState()
    data class Success(val asientos: List<AsientoSeleccionado>) : PersonDataUiState()
    data class Error(val message: String) : PersonDataUiState()
    data object Submitting : PersonDataUiState()
    data object SubmitSuccess : PersonDataUiState()
    data class SubmitError(val message: String) : PersonDataUiState()
}

