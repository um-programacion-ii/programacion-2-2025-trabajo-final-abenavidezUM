package com.eventos.backend.mapper;

import com.eventos.backend.domain.AsientoVenta;
import com.eventos.backend.dto.AsientoVentaDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AsientoVentaMapper {

    public AsientoVentaDTO toDTO(AsientoVenta asiento) {
        if (asiento == null) {
            return null;
        }

        return AsientoVentaDTO.builder()
                .id(asiento.getId())
                .fila(asiento.getFila())
                .columna(asiento.getColumna())
                .nombrePersona(asiento.getNombrePersona())
                .estado(asiento.getEstado())
                .build();
    }

    public List<AsientoVentaDTO> toDTOList(List<AsientoVenta> asientos) {
        if (asientos == null) {
            return null;
        }

        return asientos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AsientoVenta toEntity(AsientoVentaDTO dto) {
        if (dto == null) {
            return null;
        }

        return AsientoVenta.builder()
                .id(dto.getId())
                .fila(dto.getFila())
                .columna(dto.getColumna())
                .nombrePersona(dto.getNombrePersona())
                .estado(dto.getEstado())
                .build();
    }
}

