package com.eventos.backend.mapper;

import com.eventos.backend.domain.Evento;
import com.eventos.backend.dto.EventoDetalleDTO;
import com.eventos.backend.dto.EventoResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventoMapper {

    private final TipoEventoMapper tipoEventoMapper;
    private final IntegranteMapper integranteMapper;

    public EventoResumenDTO toResumenDTO(Evento evento) {
        if (evento == null) {
            return null;
        }

        Integer asientosTotales = evento.getFilaAsientos() * evento.getColumnaAsientos();

        return EventoResumenDTO.builder()
                .id(evento.getId())
                .idExterno(evento.getIdExterno())
                .titulo(evento.getTitulo())
                .resumen(evento.getResumen())
                .fecha(evento.getFecha())
                .direccion(evento.getDireccion())
                .imagen(evento.getImagen())
                .precioEntrada(evento.getPrecioEntrada())
                .tipoEvento(tipoEventoMapper.toDTO(evento.getTipoEvento()))
                .asientosTotales(asientosTotales)
                .asientosDisponibles(asientosTotales) // Se calculará con ventas después
                .build();
    }

    public EventoDetalleDTO toDetalleDTO(Evento evento) {
        if (evento == null) {
            return null;
        }

        Integer asientosTotales = evento.getFilaAsientos() * evento.getColumnaAsientos();

        return EventoDetalleDTO.builder()
                .id(evento.getId())
                .idExterno(evento.getIdExterno())
                .titulo(evento.getTitulo())
                .resumen(evento.getResumen())
                .descripcion(evento.getDescripcion())
                .fecha(evento.getFecha())
                .direccion(evento.getDireccion())
                .imagen(evento.getImagen())
                .filaAsientos(evento.getFilaAsientos())
                .columnaAsientos(evento.getColumnaAsientos())
                .precioEntrada(evento.getPrecioEntrada())
                .tipoEvento(tipoEventoMapper.toDTO(evento.getTipoEvento()))
                .integrantes(integranteMapper.toDTOList(evento.getIntegrantes()))
                .asientosTotales(asientosTotales)
                .asientosDisponibles(asientosTotales) // Se calculará con ventas después
                .createdAt(evento.getCreatedAt())
                .build();
    }

    public List<EventoResumenDTO> toResumenDTOList(List<Evento> eventos) {
        if (eventos == null) {
            return null;
        }

        return eventos.stream()
                .map(this::toResumenDTO)
                .collect(Collectors.toList());
    }
}

