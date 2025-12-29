package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la petición de bloqueo de asientos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloquearAsientosRequestDTO {
    private Long eventoId;
    private List<AsientoDTO> asientos;
}


