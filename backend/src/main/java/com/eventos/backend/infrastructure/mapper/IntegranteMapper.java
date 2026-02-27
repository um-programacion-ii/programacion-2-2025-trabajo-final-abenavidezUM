package com.eventos.backend.infrastructure.mapper;

import com.eventos.backend.domain.model.Integrante;
import com.eventos.backend.dto.IntegranteDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class IntegranteMapper {

    public IntegranteDTO toDTO(Integrante integrante) {
        if (integrante == null) {
            return null;
        }

        return IntegranteDTO.builder()
                .id(integrante.getId())
                .nombre(integrante.getNombre())
                .apellido(integrante.getApellido())
                .identificacion(integrante.getIdentificacion())
                .build();
    }

    public List<IntegranteDTO> toDTOList(List<Integrante> integrantes) {
        if (integrantes == null) {
            return null;
        }

        return integrantes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Integrante toEntity(IntegranteDTO dto) {
        if (dto == null) {
            return null;
        }

        return Integrante.builder()
                .id(dto.getId())
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .identificacion(dto.getIdentificacion())
                .build();
    }
}

