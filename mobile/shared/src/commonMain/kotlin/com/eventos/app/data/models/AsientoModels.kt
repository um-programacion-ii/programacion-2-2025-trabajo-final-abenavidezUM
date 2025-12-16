package com.eventos.app.data.models

import kotlinx.serialization.Serializable

/**
 * Estado de un asiento individual
 */
@Serializable
data class EstadoAsiento(
    val fila: Int,
    val columna: Int,
    val estado: EstadoAsientoEnum
)

@Serializable
enum class EstadoAsientoEnum {
    LIBRE,
    OCUPADO,
    BLOQUEADO,
    SELECCIONADO
}

/**
 * Mapa completo de asientos del evento
 */
@Serializable
data class MapaAsientos(
    val eventoId: Long,
    val filas: Int,
    val columnas: Int,
    val asientos: List<EstadoAsiento>
)

/**
 * Request para bloquear asientos
 */
@Serializable
data class BloquearAsientosRequest(
    val asientos: List<AsientoRequest>
)

@Serializable
data class AsientoRequest(
    val fila: Int,
    val columna: Int
)

/**
 * Response del bloqueo de asientos
 */
@Serializable
data class BloquearAsientosResponse(
    val exitoso: Boolean,
    val mensaje: String?,
    val asientosBloqueados: List<AsientoRequest>?,
    val asientosNoDisponibles: List<AsientoRequest>?
)

