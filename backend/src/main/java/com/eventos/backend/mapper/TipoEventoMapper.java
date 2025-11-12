package com.eventos.backend.mapper;

import com.eventos.backend.domain.TipoEvento;
import com.eventos.backend.dto.TipoEventoDTO;
import org.springframework.stereotype.Component;

@Component
public class TipoEventoMapper {

    public TipoEventoDTO toDTO(TipoEvento tipoEvento) {
        if (tipoEvento == null) {
            return null;
        }

        return TipoEventoDTO.builder()
                .id(tipoEvento.getId())
                .nombre(tipoEvento.getNombre())
                .descripcion(tipoEvento.getDescripcion())
                .build();
    }

    public TipoEvento toEntity(TipoEventoDTO dto) {
        if (dto == null) {
            return null;
        }

        return TipoEvento.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .build();
    }
}

