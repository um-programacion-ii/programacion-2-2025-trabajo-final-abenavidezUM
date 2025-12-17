package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para un integrante del evento
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegranteDTO {
    private String nombre;
    private String apellido;
    private String identificacion;
}

