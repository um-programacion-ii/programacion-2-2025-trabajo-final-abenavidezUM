package com.eventos.app.data.repository

import com.eventos.app.data.models.JwtResponse
import com.eventos.app.data.models.LoginRequest
import com.eventos.app.data.models.RegisterRequest
import com.eventos.app.data.models.Usuario
import com.eventos.app.data.remote.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Repositorio para operaciones de autenticación
 */
class AuthRepository {
    
    private val client = ApiClient.httpClient
    
    /**
     * Iniciar sesión
     */
    suspend fun login(username: String, password: String): Result<JwtResponse> {
        return try {
            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }
            
            val jwtResponse: JwtResponse = response.body()
            ApiClient.setAuthToken(jwtResponse.token)
            Result.success(jwtResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Registrar nuevo usuario
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        nombre: String,
        apellido: String
    ): Result<JwtResponse> {
        return try {
            val response = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password, nombre, apellido))
            }
            
            val jwtResponse: JwtResponse = response.body()
            ApiClient.setAuthToken(jwtResponse.token)
            Result.success(jwtResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cerrar sesión
     */
    suspend fun logout(): Result<Unit> {
        return try {
            client.post("/api/auth/logout")
            ApiClient.setAuthToken(null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtener usuario actual
     */
    suspend fun getCurrentUser(): Result<Usuario> {
        return try {
            val response = client.get("/api/auth/me")
            val usuario: Usuario = response.body()
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verificar si hay sesión activa
     */
    fun isAuthenticated(): Boolean {
        return ApiClient.isAuthenticated()
    }
}

