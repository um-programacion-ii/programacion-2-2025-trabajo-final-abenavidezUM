package com.eventos.backend.dto.catedra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatedraEventoCompletoDTO {
    
    private Long id;
    private String titulo;
    private String resumen;
    private String descripcion;
    private String fecha;
    private String direccion;
    private String imagen;
    private Integer filaAsientos;
    private Integer columnAsientos;
    private BigDecimal precioEntrada;
    private CatedraTipoEventoDTO eventoTipo;
    private List<CatedraIntegranteDTO> integrantes;
}

