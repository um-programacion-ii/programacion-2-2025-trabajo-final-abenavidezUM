package com.eventos.app.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.eventos.app.data.models.EventoResumen
import com.eventos.app.data.repository.EventoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventListScreenModel : ScreenModel {
    
    private val eventoRepository = EventoRepository()
    
    private val _uiState = MutableStateFlow<EventListUiState>(EventListUiState.Loading)
    val uiState: StateFlow<EventListUiState> = _uiState.asStateFlow()
    
    init {
        loadEventos()
    }
    
    fun loadEventos() {
        screenModelScope.launch {
            _uiState.value = EventListUiState.Loading
            
            val result = eventoRepository.getEventos()
            
            _uiState.value = if (result.isSuccess) {
                val eventos = result.getOrNull() ?: emptyList()
                if (eventos.isEmpty()) {
                    EventListUiState.Empty
                } else {
                    EventListUiState.Success(eventos)
                }
            } else {
                EventListUiState.Error(result.exceptionOrNull()?.message ?: "Error al cargar eventos")
            }
        }
    }
    
    fun searchEventos(query: String) {
        if (query.isBlank()) {
            loadEventos()
            return
        }
        
        screenModelScope.launch {
            _uiState.value = EventListUiState.Loading
            
            val result = eventoRepository.searchEventos(query)
            
            _uiState.value = if (result.isSuccess) {
                val eventos = result.getOrNull() ?: emptyList()
                if (eventos.isEmpty()) {
                    EventListUiState.Empty
                } else {
                    EventListUiState.Success(eventos)
                }
            } else {
                EventListUiState.Error(result.exceptionOrNull()?.message ?: "Error en búsqueda")
            }
        }
    }
}

sealed class EventListUiState {
    data object Loading : EventListUiState()
    data object Empty : EventListUiState()
    data class Success(val eventos: List<EventoResumen>) : EventListUiState()
    data class Error(val message: String) : EventListUiState()
}
