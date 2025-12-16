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
            val response = client.post("/api/ventas/realizar")
            val resultado: RealizarVentaResponse = response.body()
            Result.success(resultado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getVentas(): Result<List<Venta>> {
        return try {
            val response = client.get("/api/ventas/mis-ventas")
            val ventas: List<Venta> = response.body()
            Result.success(ventas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getVentaById(id: Long): Result<Venta> {
        return try {
            val response = client.get("/api/ventas/$id")
            val venta: Venta = response.body()
            Result.success(venta)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

