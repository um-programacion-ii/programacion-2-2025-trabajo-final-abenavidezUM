package com.eventos.app.data.models

import kotlinx.serialization.Serializable

/**
 * Sesión de compra del usuario
 */
@Serializable
data class SesionCompra(
    val eventoId: Long,
    val asientosSeleccionados: List<AsientoSeleccionado>,
    val fechaExpiracion: String
)

/**
 * Asiento seleccionado con datos de persona
 */
@Serializable
data class AsientoSeleccionado(
    val fila: Int,
    val columna: Int,
    val persona: PersonaAsiento?
)

/**
 * Datos de la persona para un asiento
 */
@Serializable
data class PersonaAsiento(
    val nombre: String,
    val apellido: String
)

/**
 * Request para iniciar sesión de compra
 */
@Serializable
data class IniciarSesionRequest(
    val eventoId: Long
)

/**
 * Request para actualizar personas en la sesión
 */
@Serializable
data class ActualizarPersonasRequest(
    val personas: List<PersonaAsientoRequest>
)

@Serializable
data class PersonaAsientoRequest(
    val fila: Int,
    val columna: Int,
    val nombre: String,
    val apellido: String
)

