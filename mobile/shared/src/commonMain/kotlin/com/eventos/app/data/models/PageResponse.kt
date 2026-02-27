package com.eventos.app.data.models

import kotlinx.serialization.Serializable

/**
 * DTO para respuestas paginadas del backend
 */
@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)


