package com.eventos.app.data.repository

import com.eventos.app.data.models.ActualizarPersonasRequest
import com.eventos.app.data.models.SesionCompra
import com.eventos.app.data.remote.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.parameter
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repositorio para gestionar sesiones de compra
 */
class SesionRepository {
    
    private val client = ApiClient.httpClient
    
    private val _sesionActual = MutableStateFlow<SesionCompra?>(null)
    val sesionActual: StateFlow<SesionCompra?> = _sesionActual.asStateFlow()
    
    /**
     * Obtiene la sesión actual (alias para obtenerSesionActual)
     */
    suspend fun getSesion(): Result<SesionCompra> {
        val result = obtenerSesionActual()
        return if (result.isSuccess) {
            val sesion = result.getOrNull()
            if (sesion != null) {
                Result.success(sesion)
            } else {
                Result.failure(Exception("No hay sesión activa"))
            }
        } else {
            result as Result<SesionCompra>
        }
    }
    
    /**
     * Actualiza los datos de personas en la sesión
     */
    suspend fun actualizarPersonas(request: ActualizarPersonasRequest): Result<SesionCompra> {
        return try {
            println("SesionRepository: Actualizando personas")
            
            val response = client.put("/api/sesion/personas") {
                contentType(ContentType.Application.Json)
                setBody(request)
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }
            
            val sesion = response.body<SesionCompra>()
            _sesionActual.value = sesion
            println("SesionRepository: Personas actualizadas")
            Result.success(sesion)
            
        } catch (e: Exception) {
            println("SesionRepository: Error al actualizar personas: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Crea una nueva sesión de compra para un evento (alias para crearSesion)
     */
    suspend fun iniciarSesion(eventoId: Long): Result<SesionCompra> {
        return crearSesion(eventoId)
    }
    
    /**
     * Crea una nueva sesión de compra para un evento
     */
    suspend fun crearSesion(eventoId: Long): Result<SesionCompra> {
        return try {
            println("SesionRepository: Creando sesión para evento $eventoId")
            
            val response = client.post("/api/sesion") {
                parameter("eventoId", eventoId)
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }
            
            val sesion = response.body<SesionCompra>()
            _sesionActual.value = sesion
            println("SesionRepository: Sesión creada: ${sesion.sesionId}")
            Result.success(sesion)
            
        } catch (e: Exception) {
            println("SesionRepository: Error al crear sesión: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene la sesión actual
     */
    suspend fun obtenerSesionActual(): Result<SesionCompra?> {
        return try {
            println("SesionRepository: Obteniendo sesión actual")
            
            val response = client.get("/api/sesion") {
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }
            
            if (response.status == HttpStatusCode.NoContent) {
                _sesionActual.value = null
                println("SesionRepository: No hay sesión activa")
                return Result.success(null)
            }
            
            val sesion = response.body<SesionCompra>()
            _sesionActual.value = sesion
            println("SesionRepository: Sesión obtenida: ${sesion.sesionId}")
            Result.success(sesion)
            
        } catch (e: Exception) {
            println("SesionRepository: Error al obtener sesión: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Limpia la sesión actual
     */
    suspend fun limpiarSesion(): Result<Unit> {
        return try {
            println("SesionRepository: Limpiando sesión")
            
            client.delete("/api/sesion") {
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }
            
            _sesionActual.value = null
            println("SesionRepository: Sesión limpiada")
            Result.success(Unit)
            
        } catch (e: Exception) {
            println("SesionRepository: Error al limpiar sesión: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Renueva la sesión actual
     */
    suspend fun renovarSesion(): Result<SesionCompra> {
        return try {
            println("SesionRepository: Renovando sesión")
            
            val response = client.put("/api/sesion/renovar") {
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }
            
            val sesion = response.body<SesionCompra>()
            _sesionActual.value = sesion
            println("SesionRepository: Sesión renovada: ${sesion.sesionId}")
            Result.success(sesion)
            
        } catch (e: Exception) {
            println("SesionRepository: Error al renovar sesión: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Limpia el estado local sin llamar al servidor
     */
    fun limpiarEstadoLocal() {
        _sesionActual.value = null
    }
}
