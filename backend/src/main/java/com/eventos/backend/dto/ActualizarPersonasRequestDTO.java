package com.eventos.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para actualizar los datos de personas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarPersonasRequestDTO {

    @NotEmpty(message = "Debe ingresar los datos de al menos una persona")
    @Valid
    private List<PersonaAsientoDTO> personas;
}

