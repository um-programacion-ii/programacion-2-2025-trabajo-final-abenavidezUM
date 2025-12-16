package com.eventos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val nombre: String,
    val apellido: String
)

@Serializable
data class JwtResponse(
    val token: String,
    val type: String = "Bearer",
    val username: String
)

@Serializable
data class Usuario(
    val id: Long,
    val username: String,
    val email: String,
    val nombre: String,
    val apellido: String,
    val activo: Boolean
)

