package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para respuesta de consulta de todos los asientos de un evento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapaAsientosResponseDTO {
    private Long eventoId;
    private Map<String, String> asientos; // Key: "fila:columna", Value: estado
    private Map<String, Long> resumen; // Conteo por estado
    private Integer totalAsientos;
    private String timestamp;
}

