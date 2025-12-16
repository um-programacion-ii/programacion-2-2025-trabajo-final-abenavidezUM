package com.eventos.app.data.repository

import com.eventos.app.data.models.ActualizarPersonasRequest
import com.eventos.app.data.models.IniciarSesionRequest
import com.eventos.app.data.models.SesionCompra
import com.eventos.app.data.remote.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class SesionRepository {
    
    private val client = ApiClient.httpClient
    
    suspend fun iniciarSesion(eventoId: Long): Result<SesionCompra> {
        return try {
            val response = client.post("/api/sesion/iniciar") {
                contentType(ContentType.Application.Json)
                setBody(IniciarSesionRequest(eventoId))
            }
            val sesion: SesionCompra = response.body()
            Result.success(sesion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSesion(): Result<SesionCompra> {
        return try {
            val response = client.get("/api/sesion")
            val sesion: SesionCompra = response.body()
            Result.success(sesion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun actualizarPersonas(request: ActualizarPersonasRequest): Result<SesionCompra> {
        return try {
            val response = client.put("/api/sesion/personas") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val sesion: SesionCompra = response.body()
            Result.success(sesion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelarSesion(): Result<Unit> {
        return try {
            client.delete("/api/sesion")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

