package com.eventos.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsientoRequestDTO {

    @NotNull(message = "La fila es obligatoria")
    @Min(value = 1, message = "La fila debe ser mayor a 0")
    private Integer fila;

    @NotNull(message = "La columna es obligatoria")
    @Min(value = 1, message = "La columna debe ser mayor a 0")
    private Integer columna;

    @NotBlank(message = "El nombre de la persona es obligatorio")
    private String nombrePersona;
}

