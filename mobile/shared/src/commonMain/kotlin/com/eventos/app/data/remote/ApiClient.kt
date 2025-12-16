package com.eventos.app.data.remote

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Cliente HTTP para comunicación con el backend
 */
object ApiClient {
    
    private const val BASE_URL = "http://localhost:8080" // TODO: Cambiar en producción
    
    private var authToken: String? = null
    
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
        }
        
        install(Auth) {
            bearer {
                loadTokens {
                    authToken?.let {
                        BearerTokens(it, it)
                    }
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
}

