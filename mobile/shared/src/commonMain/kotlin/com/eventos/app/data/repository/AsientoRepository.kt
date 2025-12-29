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
            println("AsientoRepository: Obteniendo mapa de asientos para evento $eventoId...")
            val response = client.get("/api/asientos/evento/$eventoId") {
                // ✅ Agregar token manualmente
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                    println("AsientoRepository: Token agregado: ${token.take(20)}...")
                }
            }
            val mapa: MapaAsientos = response.body()
            println("AsientoRepository: Mapa obtenido exitosamente")
            Result.success(mapa)
        } catch (e: Exception) {
            println("AsientoRepository: Error al obtener mapa: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun bloquearAsientos(
        eventoId: Long,
        request: BloquearAsientosRequest
    ): Result<BloquearAsientosResponse> {
        return try {
            println("AsientoRepository: Bloqueando asientos para evento $eventoId...")
            val response = client.post("/api/asientos/bloquear") {
                contentType(ContentType.Application.Json)
                setBody(request)
                // ✅ Agregar token manualmente
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                    println("AsientoRepository: Token agregado para bloqueo")
                }
            }
            val resultado: BloquearAsientosResponse = response.body()
            println("AsientoRepository: Asientos bloqueados exitosamente")
            Result.success(resultado)
        } catch (e: Exception) {
            println("AsientoRepository: Error al bloquear asientos: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

