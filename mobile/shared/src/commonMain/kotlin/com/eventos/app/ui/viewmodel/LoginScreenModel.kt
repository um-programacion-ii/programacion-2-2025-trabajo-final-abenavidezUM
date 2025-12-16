package com.eventos.app.ui.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.eventos.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de login
 */
class LoginScreenModel : ScreenModel {
    
    private val authRepository = AuthRepository()
    
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun login(username: String, password: String) {
        screenModelScope.launch {
            _uiState.value = LoginUiState.Loading
            
            val result = authRepository.login(username, password)
            
            _uiState.value = if (result.isSuccess) {
                LoginUiState.Success(result.getOrNull()!!.username)
            } else {
                LoginUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val username: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

