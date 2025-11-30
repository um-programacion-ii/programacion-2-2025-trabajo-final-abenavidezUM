package com.eventos.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para actualizar los asientos seleccionados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarAsientosRequestDTO {

    @NotEmpty(message = "Debe seleccionar al menos un asiento")
    @Size(max = 4, message = "No puede seleccionar más de 4 asientos")
    private List<AsientoSeleccionadoDTO> asientos;
}

