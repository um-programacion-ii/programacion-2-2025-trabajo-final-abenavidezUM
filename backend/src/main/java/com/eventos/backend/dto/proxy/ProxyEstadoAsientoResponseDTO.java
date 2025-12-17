package com.eventos.backend.dto.proxy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de consulta de estado de un asiento desde el proxy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyEstadoAsientoResponseDTO {
    private Long eventoId;
    private Integer fila;
    private Integer columna;
    private String estado; // "LIBRE", "BLOQUEADO", "VENDIDO", "OCUPADO"
    private String timestamp;
}

