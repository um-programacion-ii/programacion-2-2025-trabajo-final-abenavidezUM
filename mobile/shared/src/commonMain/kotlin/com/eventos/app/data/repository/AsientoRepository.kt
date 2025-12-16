package com.eventos.app.data.repository

import com.eventos.app.data.models.BloquearAsientosRequest
import com.eventos.app.data.models.BloquearAsientosResponse
import com.eventos.app.data.models.MapaAsientos
import com.eventos.app.data.remote.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AsientoRepository {
    
    private val client = ApiClient.httpClient
    
    suspend fun getMapaAsientos(eventoId: Long): Result<MapaAsientos> {
        return try {
            val response = client.get("/api/asientos/$eventoId/mapa")
            val mapa: MapaAsientos = response.body()
            Result.success(mapa)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun bloquearAsientos(
        eventoId: Long,
        request: BloquearAsientosRequest
    ): Result<BloquearAsientosResponse> {
        return try {
            val response = client.post("/api/asientos/$eventoId/bloquear") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val resultado: BloquearAsientosResponse = response.body()
            Result.success(resultado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

