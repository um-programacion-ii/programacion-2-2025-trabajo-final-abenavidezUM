package com.eventos.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO que representa los datos de una persona para un asiento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonaAsientoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "La fila es obligatoria")
    private Integer fila;
    
    @NotNull(message = "La columna es obligatoria")
    private Integer columna;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    private String documento;
    
    /**
     * Genera un identificador único para el asiento
     */
    public String getAsientoId() {
        return fila + "-" + columna;
    }
}

