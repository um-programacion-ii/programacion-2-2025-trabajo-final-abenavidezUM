package com.eventos.app.data.repository

import com.eventos.app.data.models.EventoDetalle
import com.eventos.app.data.models.EventoResumen
import com.eventos.app.data.models.PageResponse
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
            println("EventoRepository: Obteniendo eventos...")
            val response = client.get("/api/eventos") {
                parameter("page", page)
                parameter("size", size)
                // ✅ Agregar token manualmente
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                    println("EventoRepository: Token agregado: ${token.take(20)}...")
                }
            }
            
            // Backend devuelve Page<EventoResumenDTO>
            val pageResponse: PageResponse<EventoResumen> = response.body()
            println("EventoRepository: ${pageResponse.content.size} eventos obtenidos exitosamente")
            Result.success(pageResponse.content)
        } catch (e: Exception) {
            println("EventoRepository: Error al obtener eventos: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Obtener detalle de un evento
     */
    suspend fun getEventoById(id: Long): Result<EventoDetalle> {
        return try {
            val response = client.get("/api/eventos/$id") {
                // ✅ Agregar token manualmente
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
            }
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
                // ✅ Agregar token manualmente
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
            }
            
            val pageResponse: PageResponse<EventoResumen> = response.body()
            Result.success(pageResponse.content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

