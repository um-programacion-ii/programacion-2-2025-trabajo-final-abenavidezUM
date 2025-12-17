package com.eventos.proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para evento completo (con todos los datos)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoCompletoDTO {
    private Long id;
    private String titulo;
    private String resumen;
    private String descripcion;
    private String fecha;
    private String direccion;
    private String imagen;
    private Integer filaAsientos;
    private Integer columnAsientos;
    private Double precioEntrada;
    private EventoTipoDTO eventoTipo;
    private List<IntegranteDTO> integrantes;
}

