package com.eventos.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoDetalleDTO {

    private Long id;
    private Long idExterno;
    private String titulo;
    private String resumen;
    private String descripcion;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha;
    
    private String direccion;
    private String imagen;
    private Integer filaAsientos;
    private Integer columnaAsientos;
    private BigDecimal precioEntrada;
    private TipoEventoDTO tipoEvento;
    private List<IntegranteDTO> integrantes;
    private Integer asientosDisponibles;
    private Integer asientosTotales;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

