package com.eventos.app.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.eventos.app.data.models.EventoDetalle
import com.eventos.app.data.repository.EventoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de detalle de evento
 */
class EventDetailScreenModel(private val eventoId: Long) : ScreenModel {
    
    private val eventoRepository = EventoRepository()
    
    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadEvento()
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

