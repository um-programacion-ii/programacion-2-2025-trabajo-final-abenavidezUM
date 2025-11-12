package com.eventos.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearVentaRequestDTO {

    @NotNull(message = "El ID del evento es obligatorio")
    private Long eventoId;

    @NotEmpty(message = "Debe seleccionar al menos un asiento")
    @Valid
    private List<AsientoRequestDTO> asientos;
}

