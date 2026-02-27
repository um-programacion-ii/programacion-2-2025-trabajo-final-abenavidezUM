package com.eventos.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que representa el mapa completo de asientos de un evento
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapaAsientosDTO {

    private Long eventoId;
    private Integer totalFilas;
    private Integer totalColumnas;
    private Integer asientosTotales;
    private Integer asientosLibres;
    private Integer asientosOcupados;
    private Integer asientosBloqueados;
    private List<EstadoAsientoDTO> asientos;
}

