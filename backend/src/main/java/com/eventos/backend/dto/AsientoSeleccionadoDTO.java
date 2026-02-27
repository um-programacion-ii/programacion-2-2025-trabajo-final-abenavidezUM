package com.eventos.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO que representa un asiento seleccionado en la sesión de compra
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsientoSeleccionadoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer fila;
    private Integer columna;
    
    /**
     * Genera un identificador único para el asiento
     */
    public String getId() {
        return fila + "-" + columna;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsientoSeleccionadoDTO that = (AsientoSeleccionadoDTO) o;
        return fila.equals(that.fila) && columna.equals(that.columna);
    }
    
    @Override
    public int hashCode() {
        return 31 * fila + columna;
    }
}

