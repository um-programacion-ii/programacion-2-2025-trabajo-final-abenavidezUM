package com.eventos.app.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.eventos.app.data.models.SesionCompra
import com.eventos.app.data.repository.SesionRepository
import com.eventos.app.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConfirmationScreenModel : ScreenModel {
    
    private val sesionRepository = SesionRepository()
    private val ventaRepository = VentaRepository()
    
    private val _uiState = MutableStateFlow<ConfirmationUiState>(ConfirmationUiState.Loading)
    val uiState: StateFlow<ConfirmationUiState> = _uiState.asStateFlow()
    
    init {
        loadSesion()
    }
    
    private fun loadSesion() {
        screenModelScope.launch {
            _uiState.value = ConfirmationUiState.Loading
            
            val result = sesionRepository.getSesion()
            
            _uiState.value = if (result.isSuccess) {
                ConfirmationUiState.Success(result.getOrNull()!!)
            } else {
                ConfirmationUiState.Error(result.exceptionOrNull()?.message ?: "Error al cargar sesión")
            }
        }
    }
    
    fun confirmarCompra() {
        screenModelScope.launch {
            _uiState.value = ConfirmationUiState.Processing
            
            val result = ventaRepository.realizarVenta()
            
            _uiState.value = if (result.isSuccess) {
                val response = result.getOrNull()!!
                if (response.exitoso && response.venta != null) {
                    ConfirmationUiState.PurchaseSuccess(response.venta!!)
                } else {
                    ConfirmationUiState.PurchaseError(response.mensaje ?: "Error al procesar la compra")
                }
            } else {
                ConfirmationUiState.PurchaseError(result.exceptionOrNull()?.message ?: "Error al procesar la compra")
            }
        }
    }
    
    fun retry() {
        loadSesion()
    }
    
    fun resetPurchaseState() {
        loadSesion()
    }
}

sealed class ConfirmationUiState {
    data object Loading : ConfirmationUiState()
    data class Success(val sesion: SesionCompra) : ConfirmationUiState()
    data class Error(val message: String) : ConfirmationUiState()
    data object Processing : ConfirmationUiState()
    data class PurchaseSuccess(val venta: com.eventos.app.data.models.Venta) : ConfirmationUiState()
    data class PurchaseError(val message: String) : ConfirmationUiState()
}

