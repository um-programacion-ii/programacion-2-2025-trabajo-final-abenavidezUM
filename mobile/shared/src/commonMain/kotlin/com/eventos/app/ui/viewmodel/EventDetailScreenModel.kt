package com.eventos.app.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.eventos.app.data.models.EventoDetalle
import com.eventos.app.data.repository.EventoRepository
import com.eventos.app.data.repository.SesionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle de evento
 */
class EventDetailScreenModel(private val eventoId: Long) : ScreenModel {
    
    private val eventoRepository = EventoRepository()
    private val sesionRepository = SesionRepository()
    
    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadEvento()
        crearSesionParaEvento()
    }
    
    /**
     * Crea una sesión de compra al seleccionar el evento
     * Limpia cualquier sesión anterior primero para empezar fresh
     */
    private fun crearSesionParaEvento() {
        screenModelScope.launch {
            try {
                // Primero limpiar cualquier sesión existente
                println("EventDetailScreenModel: Limpiando sesión anterior...")
                sesionRepository.limpiarSesion()
                
                // Ahora crear la nueva sesión
                println("EventDetailScreenModel: Creando sesión nueva para evento $eventoId")
                val result = sesionRepository.crearSesion(eventoId)
                if (result.isSuccess) {
                    println("EventDetailScreenModel: Sesión creada exitosamente")
                } else {
                    println("EventDetailScreenModel: Error al crear sesión: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                println("EventDetailScreenModel: Error al crear sesión: ${e.message}")
            }
        }
    }
    
    private fun loadEvento() {
        screenModelScope.launch {
            _uiState.value = EventDetailUiState.Loading
            
            val result = eventoRepository.getEventoById(eventoId)
            
            _uiState.value = if (result.isSuccess) {
                EventDetailUiState.Success(result.getOrNull()!!)
            } else {
                EventDetailUiState.Error(result.exceptionOrNull()?.message ?: "Error al cargar evento")
            }
        }
    }
    
    fun retry() {
        loadEvento()
    }
}

sealed class EventDetailUiState {
    data object Loading : EventDetailUiState()
    data class Success(val evento: EventoDetalle) : EventDetailUiState()
    data class Error(val message: String) : EventDetailUiState()
}

