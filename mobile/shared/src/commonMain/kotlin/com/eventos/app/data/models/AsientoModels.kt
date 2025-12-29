package com.eventos.app.data.models

import kotlinx.serialization.SerialName
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
    @SerialName("totalFilas") val filas: Int,
    @SerialName("totalColumnas") val columnas: Int,
    @SerialName("asientosTotales") val asientosTotales: Int? = null,
    @SerialName("asientosLibres") val asientosLibres: Int? = null,
    @SerialName("asientosOcupados") val asientosOcupados: Int? = null,
    @SerialName("asientosBloqueados") val asientosBloqueados: Int? = null,
    val asientos: List<EstadoAsiento>
)

/**
 * Request para bloquear asientos
 */
@Serializable
data class BloquearAsientosRequest(
    val eventoId: Long,
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
    val asientosBloqueados: List<AsientoRequest>? = null,
    val expiracion: String? = null  // ✅ Campo del backend
)

