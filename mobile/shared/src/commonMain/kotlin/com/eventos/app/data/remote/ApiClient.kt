package com.eventos.app.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json

/**
 * Cliente HTTP para comunicación con el backend
 */
object ApiClient {
    
    // IMPORTANTE: 10.0.2.2 es la IP para acceder al localhost del host desde el emulador
    private const val BASE_URL = "http://10.0.2.2:8080"
    
    private var authToken: String? = null
    
    // Flow para notificar cuando la sesión expira (401)
    private val _sessionExpiredFlow = MutableSharedFlow<Unit>()
    val sessionExpiredFlow: SharedFlow<Unit> = _sessionExpiredFlow.asSharedFlow()
    
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.ALL
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 15000  // Reducido a 15s para debug
            connectTimeoutMillis = 10000   // Reducido a 10s para debug
            socketTimeoutMillis = 15000    // Reducido a 15s para debug
        }
        
        // Interceptor para detectar 401 Unauthorized
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status == HttpStatusCode.Unauthorized) {
                    // Limpiar token
                    authToken = null
                    // Notificar que la sesión expiró
                    _sessionExpiredFlow.emit(Unit)
                }
            }
        }
        
        defaultRequest {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }
    
    fun setAuthToken(token: String?) {
        authToken = token
    }
    
    fun getAuthToken(): String? = authToken
    
    fun isAuthenticated(): Boolean = authToken != null
    
    fun clearSession() {
        authToken = null
    }
}

