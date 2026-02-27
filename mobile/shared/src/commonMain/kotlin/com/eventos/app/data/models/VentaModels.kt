package com.eventos.app.data.models

import kotlinx.serialization.Serializable

/**
 * Venta completa - coincide con VentaDTO del backend
 */
@Serializable
data class Venta(
    val id: Long,
    val idExterno: Long? = null,
    val eventoId: Long,
    val eventoTitulo: String? = null,
    val fechaVenta: String,
    val precioTotal: Double,
    val resultado: Boolean? = null,
    val descripcion: String? = null,
    val confirmadaCatedra: Boolean? = null,
    val asientos: List<AsientoVenta>,
    val createdAt: String? = null
)

/**
 * Asiento vendido - coincide con AsientoVentaDTO del backend
 */
@Serializable
data class AsientoVenta(
    val id: Long? = null,
    val fila: Int,
    val columna: Int,
    val nombrePersona: String,
    val apellidoPersona: String,
    val precio: Double,
    val estado: String? = null
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


