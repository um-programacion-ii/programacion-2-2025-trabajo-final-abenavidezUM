package com.eventos.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoResumenDTO {

    private Long id;
    private Long idExterno;
    private String titulo;
    private String resumen;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha;
    
    private String direccion;
    private String imagen;
    private BigDecimal precioEntrada;
    private TipoEventoDTO tipoEvento;
    private Integer asientosDisponibles;
    private Integer asientosTotales;
}

