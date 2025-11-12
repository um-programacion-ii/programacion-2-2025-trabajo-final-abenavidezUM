package com.eventos.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegranteDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String identificacion;
}

