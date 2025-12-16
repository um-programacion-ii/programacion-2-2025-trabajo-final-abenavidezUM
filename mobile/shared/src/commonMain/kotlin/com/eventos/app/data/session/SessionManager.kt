package com.eventos.app.data.session

import com.eventos.app.data.models.SesionCompra
import com.eventos.app.data.repository.SesionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gestor de sesión de compra
 * Maneja sincronización, verificación y expiración de sesiones
 */
class SessionManager {
    
    private val sesionRepository = SesionRepository()
    private val scope = CoroutineScope(Dispatchers.Default)
    
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.NoSession)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    private var pollingJob: Job? = null
    
    /**
     * Verifica si hay una sesión activa al iniciar la app
     */
    suspend fun checkActiveSession(): SessionState {
        val result = sesionRepository.getSesion()
        
        val state = if (result.isSuccess) {
            val sesion = result.getOrNull()
            if (sesion != null) {
                // Verificar si la sesión está expirada
                if (isSessionExpired(sesion)) {
                    SessionState.Expired
                } else {
                    SessionState.Active(sesion)
                }
            } else {
                SessionState.NoSession
            }
        } else {
            // Si hay error 404 o similar, no hay sesión
            SessionState.NoSession
        }
        
        _sessionState.value = state
        return state
    }
    
    /**
     * Inicia el monitoreo periódico de la sesión
     */
    fun startSessionPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (true) {
                delay(30000) // Verificar cada 30 segundos
                checkActiveSession()
            }
        }
    }
    
    /**
     * Detiene el monitoreo de la sesión
     */
    fun stopSessionPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }
    
    /**
     * Limpia el estado de la sesión
     */
    fun clearSession() {
        _sessionState.value = SessionState.NoSession
        stopSessionPolling()
    }
    
    /**
     * Verifica si una sesión está expirada
     */
    private fun isSessionExpired(sesion: SesionCompra): Boolean {
        // TODO: Implementar verificación de fecha de expiración
        // Por ahora retorna false, la verificación real se hace en el backend
        return false
    }
    
    companion object {
        @Volatile
        private var instance: SessionManager? = null
        
        fun getInstance(): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager().also { instance = it }
            }
        }
    }
}

sealed class SessionState {
    data object NoSession : SessionState()
    data class Active(val sesion: SesionCompra) : SessionState()
    data object Expired : SessionState()
}

