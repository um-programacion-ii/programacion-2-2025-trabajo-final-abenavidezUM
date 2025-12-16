package com.eventos.app.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.eventos.app.data.models.Venta
import com.eventos.app.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesHistoryScreenModel : ScreenModel {
    
    private val ventaRepository = VentaRepository()
    
    private val _uiState = MutableStateFlow<SalesHistoryUiState>(SalesHistoryUiState.Loading)
    val uiState: StateFlow<SalesHistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadVentas()
    }
    
    fun loadVentas() {
        screenModelScope.launch {
            _uiState.value = SalesHistoryUiState.Loading
            
            val result = ventaRepository.getVentas()
            
            _uiState.value = if (result.isSuccess) {
                val ventas = result.getOrNull() ?: emptyList()
                if (ventas.isEmpty()) {
                    SalesHistoryUiState.Empty
                } else {
                    SalesHistoryUiState.Success(ventas)
                }
            } else {
                SalesHistoryUiState.Error(result.exceptionOrNull()?.message ?: "Error al cargar ventas")
            }
        }
    }
}

sealed class SalesHistoryUiState {
    data object Loading : SalesHistoryUiState()
    data object Empty : SalesHistoryUiState()
    data class Success(val ventas: List<Venta>) : SalesHistoryUiState()
    data class Error(val message: String) : SalesHistoryUiState()
}

