package com.eventos.app.data.models

import kotlinx.serialization.Serializable

/**
 * Venta completa
 */
@Serializable
data class Venta(
    val id: Long,
    val eventoId: Long,
    val usuarioId: Long,
    val fechaVenta: String,
    val montoTotal: Double,
    val estado: EstadoVenta,
    val asientos: List<AsientoVenta>
)

@Serializable
enum class EstadoVenta {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA
}

/**
 * Asiento vendido
 */
@Serializable
data class AsientoVenta(
    val fila: Int,
    val columna: Int,
    val nombrePersona: String,
    val apellidoPersona: String,
    val precio: Double
)

/**
 * Response de realizar venta
 */
@Serializable
data class RealizarVentaResponse(
    val exitoso: Boolean,
    val mensaje: String?,
    val venta: Venta?
)

