package com.eventos.app.data.repository

import com.eventos.app.data.models.EventoDetalle
import com.eventos.app.data.models.EventoResumen
import com.eventos.app.data.remote.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*

class EventoRepository {
    
    private val client = ApiClient.httpClient
    
    suspend fun getEventos(page: Int = 0, size: Int = 20): Result<List<EventoResumen>> {
        return try {
            val response = client.get("/api/eventos") {
                parameter("page", page)
                parameter("size", size)
            }
            
            // El backend devuelve Page<EventoResumenDTO>
            val jsonElement: JsonElement = response.body()
            val jsonObject = jsonElement.jsonObject
            val contentArray = jsonObject["content"]?.jsonArray
            
            val eventos = contentArray?.map { element ->
                Json.decodeFromJsonElement<EventoResumen>(element)
            } ?: emptyList()
            
            Result.success(eventos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getEventoById(id: Long): Result<EventoDetalle> {
        return try {
            val response = client.get("/api/eventos/$id")
            val evento: EventoDetalle = response.body()
            Result.success(evento)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchEventos(query: String, page: Int = 0, size: Int = 20): Result<List<EventoResumen>> {
        return try {
            val response = client.get("/api/eventos/search") {
                parameter("q", query)
                parameter("page", page)
                parameter("size", size)
            }
            
            val jsonElement: JsonElement = response.body()
            val jsonObject = jsonElement.jsonObject
            val contentArray = jsonObject["content"]?.jsonArray
            
            val eventos = contentArray?.map { element ->
                Json.decodeFromJsonElement<EventoResumen>(element)
            } ?: emptyList()
            
            Result.success(eventos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
