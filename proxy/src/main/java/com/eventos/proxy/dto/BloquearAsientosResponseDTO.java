package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la respuesta de bloqueo de asientos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloquearAsientosResponseDTO {
    private Boolean resultado;
    private String descripcion;
    private Long eventoId;
    private List<AsientoEstadoDTO> asientos;
}

