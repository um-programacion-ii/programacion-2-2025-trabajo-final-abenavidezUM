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
            println("AuthRepository: Iniciando login para usuario: $username")
            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }
            
            println("AuthRepository: Respuesta recibida, status: ${response.status}")
            
            // Validar status code antes de deserializar
            if (response.status == HttpStatusCode.Unauthorized) {
                println("AuthRepository: Credenciales incorrectas (401)")
                return Result.failure(Exception("Usuario o contraseña incorrectos"))
            }
            
            if (!response.status.isSuccess()) {
                println("AuthRepository: Error del servidor: ${response.status}")
                return Result.failure(Exception("Error en el servidor: ${response.status.value}"))
            }
            
            val jwtResponse: JwtResponse = response.body()
            ApiClient.setAuthToken(jwtResponse.token)
            println("AuthRepository: Login exitoso!")
            Result.success(jwtResponse)
        } catch (e: Exception) {
            println("AuthRepository: Error en login: ${e.message}")
            e.printStackTrace()
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
        firstName: String,
        lastName: String
    ): Result<JwtResponse> {
        return try {
            println("AuthRepository: Iniciando registro para usuario: $username")
            val response = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password, firstName, lastName))
            }
            
            println("AuthRepository: Respuesta recibida, status: ${response.status}")
            
            if (!response.status.isSuccess()) {
                println("AuthRepository: Error del servidor: ${response.status}")
                return Result.failure(Exception("Error en el servidor: ${response.status.value}"))
            }
            
            val jwtResponse: JwtResponse = response.body()
            ApiClient.setAuthToken(jwtResponse.token)
            println("AuthRepository: Registro exitoso!")
            Result.success(jwtResponse)
        } catch (e: Exception) {
            println("AuthRepository: Error en registro: ${e.message}")
            e.printStackTrace()
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

