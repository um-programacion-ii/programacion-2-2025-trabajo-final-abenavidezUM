package com.eventos.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para iniciar una sesión de compra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IniciarSesionRequestDTO {

    @NotNull(message = "El ID del evento es obligatorio")
    private Long eventoId;
}


