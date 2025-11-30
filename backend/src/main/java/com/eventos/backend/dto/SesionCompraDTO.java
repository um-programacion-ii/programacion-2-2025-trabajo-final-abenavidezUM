package com.eventos.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa una sesión de compra en Redis.
 * Contiene el estado completo del proceso de compra del usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionCompraDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sesionId;
    private Long usuarioId;
    private Long eventoId;
    private Long eventoIdExterno;
    private String eventoTitulo;
    private BigDecimal precioUnitario;
    
    @Builder.Default
    private List<AsientoSeleccionadoDTO> asientosSeleccionados = new ArrayList<>();
    
    @Builder.Default
    private List<PersonaAsientoDTO> personas = new ArrayList<>();
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private Boolean asientosBloqueados;
    
    /**
     * Calcula el precio total basado en los asientos seleccionados
     */
    public BigDecimal getPrecioTotal() {
        if (precioUnitario == null || asientosSeleccionados == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(BigDecimal.valueOf(asientosSeleccionados.size()));
    }
    
    /**
     * Verifica si la sesión está completa (tiene asientos y personas)
     */
    public boolean isCompleta() {
        return asientosSeleccionados != null 
                && !asientosSeleccionados.isEmpty()
                && personas != null 
                && personas.size() == asientosSeleccionados.size();
    }
    
    /**
     * Verifica si la sesión ha expirado
     */
    public boolean isExpirada() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}

