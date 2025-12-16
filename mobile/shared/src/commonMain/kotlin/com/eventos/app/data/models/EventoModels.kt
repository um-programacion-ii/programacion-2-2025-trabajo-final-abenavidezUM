package com.eventos.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class EventoResumen(
    val id: Long,
    val titulo: String,
    val descripcion: String?,
    val fecha: String,
    val lugar: String,
    val precio: Double,
    val capacidadTotal: Int,
    val asientosDisponibles: Int,
    val imagenUrl: String?,
    val tipoEvento: TipoEvento
)

@Serializable
data class EventoDetalle(
    val id: Long,
    val idExterno: Long?,
    val titulo: String,
    val descripcion: String?,
    val fecha: String,
    val lugar: String,
    val precio: Double,
    val capacidadTotal: Int,
    val filas: Int,
    val columnas: Int,
    val imagenUrl: String?,
    val tipoEvento: TipoEvento,
    val integrantes: List<Integrante>,
    val asientosDisponibles: Int,
    val activo: Boolean
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
    val rol: String?
)

