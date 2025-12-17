package com.eventos.backend.dto.proxy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para respuesta de mapa completo de asientos desde el proxy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyMapaAsientosResponseDTO {
    private Long eventoId;
    private Map<String, String> asientos; // Key: "fila:columna", Value: estado
    private Map<String, Long> resumen; // Conteo por estado
    private Integer totalAsientos;
    private String timestamp;
}

