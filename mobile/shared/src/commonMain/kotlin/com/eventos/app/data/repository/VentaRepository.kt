package com.eventos.app.data.repository

import com.eventos.app.data.models.RealizarVentaResponse
import com.eventos.app.data.models.Venta
import com.eventos.app.data.remote.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*

class VentaRepository {
    
    private val client = ApiClient.httpClient
    
    suspend fun realizarVenta(): Result<RealizarVentaResponse> {
        return try {
            println("VentaRepository: Realizando venta...")
            val response = client.post("/api/ventas") {
                // ✅ Agregar token manualmente
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
            }
            
            // Verificar si la respuesta es exitosa
            if (response.status.value !in 200..299) {
                val errorMessage = when (response.status.value) {
                    401 -> "No autorizado para realizar la venta. Por favor, inicie sesión nuevamente."
                    500 -> "Error interno del servidor al procesar la venta. Verifique que el backend esté corriendo correctamente."
                    else -> "Error al realizar la venta: ${response.status.description}"
                }
                println("VentaRepository: $errorMessage")
                return Result.failure(Exception(errorMessage))
            }
            
            val venta: Venta = response.body()
            println("VentaRepository: Venta realizada exitosamente, ID: ${venta.id}")
            Result.success(RealizarVentaResponse(
                exitoso = true,
                mensaje = "Venta realizada exitosamente",
                venta = venta
            ))
        } catch (e: Exception) {
            println("VentaRepository: Error al realizar venta: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun getVentas(): Result<List<Venta>> {
        return try {
            val response = client.get("/api/ventas") {
                // ✅ Agregar token manualmente
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
            }
            val ventas: List<Venta> = response.body()
            Result.success(ventas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getVentaById(id: Long): Result<Venta> {
        return try {
            val response = client.get("/api/ventas/$id") {
                // ✅ Agregar token manualmente
                ApiClient.getAuthToken()?.let { token ->
                    headers {
                        append("Authorization", "Bearer $token")
                    }
                }
            }
            val venta: Venta = response.body()
            Result.success(venta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

