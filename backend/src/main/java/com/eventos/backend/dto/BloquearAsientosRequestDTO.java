package com.eventos.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para solicitar bloqueo de asientos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloquearAsientosRequestDTO {

    @NotNull(message = "El ID del evento es obligatorio")
    private Long eventoId;

    @NotEmpty(message = "Debe seleccionar al menos un asiento")
    @Size(max = 4, message = "No puede bloquear más de 4 asientos")
    private List<AsientoSeleccionadoDTO> asientos;
}

