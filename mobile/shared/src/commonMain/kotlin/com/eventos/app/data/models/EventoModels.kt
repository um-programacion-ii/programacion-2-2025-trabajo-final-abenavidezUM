package com.eventos.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventoResumen(
    val id: Long,
    val idExterno: Long?,
    val titulo: String,
    @SerialName("resumen") val descripcion: String?,
    val fecha: String,
    @SerialName("direccion") val lugar: String,
    @SerialName("imagen") val imagenUrl: String?,
    @SerialName("precioEntrada") val precio: Double,
    val tipoEvento: TipoEvento,
    val asientosDisponibles: Int,
    @SerialName("asientosTotales") val capacidadTotal: Int
)

@Serializable
data class EventoDetalle(
    val id: Long,
    val idExterno: Long?,
    val titulo: String,
    val resumen: String?,
    val descripcion: String?,
    val fecha: String,
    @SerialName("direccion") val lugar: String,
    @SerialName("imagen") val imagenUrl: String?,
    @SerialName("precioEntrada") val precio: Double,
    @SerialName("asientosTotales") val capacidadTotal: Int,
    @SerialName("filaAsientos") val filas: Int? = 10,
    @SerialName("columnaAsientos") val columnas: Int? = 6,
    val tipoEvento: TipoEvento,
    val integrantes: List<Integrante>,
    val asientosDisponibles: Int,
    val activo: Boolean? = true,
    val createdAt: String? = null
)

@Serializable
data class TipoEvento(
    val id: Long,
    val nombre: String,
    val descripcion: String?
)

@Serializable
data class Integrante(
    val id: Long,
    val nombre: String,
    val apellido: String?,
    @SerialName("identificacion") val rol: String?
)

