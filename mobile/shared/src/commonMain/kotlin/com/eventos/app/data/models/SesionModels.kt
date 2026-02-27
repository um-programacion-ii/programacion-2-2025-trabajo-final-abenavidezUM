package com.eventos.app.data.models

import kotlinx.serialization.Serializable

/**
 * Sesión de compra del usuario
 */
@Serializable
data class SesionCompra(
    val sesionId: String,
    val usuarioId: Long,
    val eventoId: Long,
    val eventoIdExterno: Long,
    val eventoTitulo: String,
    val precioUnitario: Double,
    val asientosSeleccionados: List<AsientoSeleccionado> = emptyList(),
    val personas: List<PersonaAsiento> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val expiresAt: String,
    val asientosBloqueados: Boolean = false,
    val fechaExpiracion: String? = null  // Alias para expiresAt (para UI)
)

/**
 * Asiento seleccionado en la sesión
 */
@Serializable
data class AsientoSeleccionado(
    val fila: Int,
    val columna: Int,
    val persona: PersonaAsiento? = null
)

/**
 * Datos de una persona para un asiento
 */
@Serializable
data class PersonaAsiento(
    val fila: Int,
    val columna: Int,
    val nombre: String,
    val apellido: String,
    val documento: String? = null,
    val asientoId: String? = null
)

/**
 * Request para actualizar personas en la sesión
 */
@Serializable
data class ActualizarPersonasRequest(
    val personas: List<PersonaAsientoRequest>
)

/**
 * Persona para un asiento específico
 */
@Serializable
data class PersonaAsientoRequest(
    val fila: Int,
    val columna: Int,
    val nombre: String,
    val apellido: String
)
