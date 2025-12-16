package com.eventos.app.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.eventos.app.data.models.*
import com.eventos.app.data.repository.AsientoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SeatMapScreenModel(private val eventoId: Long) : ScreenModel {
    
    private val asientoRepository = AsientoRepository()
    
    private val _uiState = MutableStateFlow<SeatMapUiState>(SeatMapUiState.Loading)
    val uiState: StateFlow<SeatMapUiState> = _uiState.asStateFlow()
    
    private val _selectedSeats = MutableStateFlow<Set<AsientoRequest>>(emptySet())
    val selectedSeats: StateFlow<Set<AsientoRequest>> = _selectedSeats.asStateFlow()
    
    init {
        loadMapaAsientos()
    }
    
    private fun loadMapaAsientos() {
        screenModelScope.launch {
            _uiState.value = SeatMapUiState.Loading
            
            val result = asientoRepository.getMapaAsientos(eventoId)
            
            _uiState.value = if (result.isSuccess) {
                SeatMapUiState.Success(result.getOrNull()!!)
            } else {
                SeatMapUiState.Error(result.exceptionOrNull()?.message ?: "Error al cargar mapa")
            }
        }
    }
    
    fun toggleSeatSelection(fila: Int, columna: Int) {
        val currentSeats = _selectedSeats.value.toMutableSet()
        val seat = AsientoRequest(fila, columna)
        
        if (currentSeats.contains(seat)) {
            currentSeats.remove(seat)
        } else {
            if (currentSeats.size < 4) {
                currentSeats.add(seat)
            }
        }
        
        _selectedSeats.value = currentSeats
    }
    
    fun bloquearAsientos() {
        val seatsToBlock = _selectedSeats.value.toList()
        
        if (seatsToBlock.isEmpty()) {
            return
        }
        
        screenModelScope.launch {
            _uiState.value = SeatMapUiState.Blocking
            
            val request = BloquearAsientosRequest(seatsToBlock)
            val result = asientoRepository.bloquearAsientos(eventoId, request)
            
            _uiState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                if (response.exitoso) {
                    SeatMapUiState.BlockSuccess
                } else {
                    SeatMapUiState.BlockError(response.mensaje ?: "Error al bloquear asientos")
                }
            } else {
                SeatMapUiState.BlockError(result.exceptionOrNull()?.message ?: "Error al bloquear asientos")
            }
        }
    }
    
    fun retry() {
        loadMapaAsientos()
    }
    
    fun resetBlockState() {
        loadMapaAsientos()
    }
}

sealed class SeatMapUiState {
    data object Loading : SeatMapUiState()
    data class Success(val mapa: MapaAsientos) : SeatMapUiState()
    data class Error(val message: String) : SeatMapUiState()
    data object Blocking : SeatMapUiState()
    data object BlockSuccess : SeatMapUiState()
    data class BlockError(val message: String) : SeatMapUiState()
}

