package com.eventos.app.data.repository

import com.eventos.app.data.models.EventoDetalle
import com.eventos.app.data.models.EventoResumen
import com.eventos.app.data.remote.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*

/**
 * Repositorio para operaciones con eventos
 */
class EventoRepository {
    
    private val client = ApiClient.httpClient
    
    /**
     * Obtener listado de eventos
     */
    suspend fun getEventos(page: Int = 0, size: Int = 10): Result<List<EventoResumen>> {
        return try {
            val response = client.get("/api/eventos") {
                parameter("page", page)
                parameter("size", size)
            }
            
            // Asumiendo que el backend devuelve Page<EventoResumenDTO>
            val data: Map<String, Any> = response.body()
            val content = data["content"] as? List<*>
            
            val eventos = content?.mapNotNull { it as? EventoResumen } ?: emptyList()
            Result.success(eventos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener detalle de un evento
     */
    suspend fun getEventoById(id: Long): Result<EventoDetalle> {
        return try {
            val response = client.get("/api/eventos/$id")
            val evento: EventoDetalle = response.body()
            Result.success(evento)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Buscar eventos por título
     */
    suspend fun searchEventos(query: String, page: Int = 0, size: Int = 10): Result<List<EventoResumen>> {
        return try {
            val response = client.get("/api/eventos/search") {
                parameter("q", query)
                parameter("page", page)
                parameter("size", size)
            }
            
            val data: Map<String, Any> = response.body()
            val content = data["content"] as? List<*>
            
            val eventos = content?.mapNotNull { it as? EventoResumen } ?: emptyList()
            Result.success(eventos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

