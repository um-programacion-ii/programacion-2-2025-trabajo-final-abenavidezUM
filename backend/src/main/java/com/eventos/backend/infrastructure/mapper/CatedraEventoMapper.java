package com.eventos.backend.infrastructure.mapper;

import com.eventos.backend.domain.model.Evento;
import com.eventos.backend.domain.model.Integrante;
import com.eventos.backend.domain.model.TipoEvento;
import com.eventos.backend.dto.catedra.CatedraEventoCompletoDTO;
import com.eventos.backend.dto.catedra.CatedraIntegranteDTO;
import com.eventos.backend.infrastructure.adapter.output.persistence.repository.TipoEventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CatedraEventoMapper {

    private final TipoEventoRepository tipoEventoRepository;

    /**
     * Convierte un evento de cátedra a una entidad local
     */
    public Evento toEntity(CatedraEventoCompletoDTO catedraEvento) {
        if (catedraEvento == null) {
            return null;
        }

        try {
            // Buscar o crear el tipo de evento
            TipoEvento tipoEvento = null;
            if (catedraEvento.getEventoTipo() != null) {
                String nombreTipo = catedraEvento.getEventoTipo().getNombre();
                tipoEvento = tipoEventoRepository.findByNombre(nombreTipo)
                        .orElseGet(() -> {
                            TipoEvento nuevoTipo = TipoEvento.builder()
                                    .nombre(nombreTipo)
                                    .descripcion(catedraEvento.getEventoTipo().getDescripcion())
                                    .build();
                            return tipoEventoRepository.save(nuevoTipo);
                        });
            }

            // Parsear fecha
            ZonedDateTime fecha = null;
            if (catedraEvento.getFecha() != null) {
                try {
                    fecha = ZonedDateTime.parse(catedraEvento.getFecha(), DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception e) {
                    log.warn("Error al parsear fecha del evento {}: {}", catedraEvento.getId(), e.getMessage());
                }
            }

            // Crear el evento
            // Usar valores por defecto si no están especificados (10 filas x 16 columnas)
            Integer filas = catedraEvento.getFilaAsientos() != null ? catedraEvento.getFilaAsientos() : 10;
            Integer columnas = catedraEvento.getColumnAsientos() != null ? catedraEvento.getColumnAsientos() : 16;
            
            Evento evento = Evento.builder()
                    .idExterno(catedraEvento.getId())
                    .titulo(catedraEvento.getTitulo())
                    .resumen(catedraEvento.getResumen())
                    .descripcion(catedraEvento.getDescripcion())
                    .fecha(fecha != null ? fecha.toLocalDateTime() : null)
                    .direccion(catedraEvento.getDireccion())
                    .imagen(catedraEvento.getImagen())
                    .filaAsientos(filas)
                    .columnaAsientos(columnas)
                    .precioEntrada(catedraEvento.getPrecioEntrada())
                    .tipoEvento(tipoEvento)
                    .activo(true)
                    .integrantes(new ArrayList<>())
                    .build();

            // Agregar integrantes
            if (catedraEvento.getIntegrantes() != null) {
                for (CatedraIntegranteDTO catedraIntegrante : catedraEvento.getIntegrantes()) {
                    Integrante integrante = Integrante.builder()
                            .nombre(catedraIntegrante.getNombre())
                            .apellido(catedraIntegrante.getApellido())
                            .identificacion(catedraIntegrante.getIdentificacion())
                            .build();
                    evento.addIntegrante(integrante);
                }
            }

            return evento;

        } catch (Exception e) {
            log.error("Error al convertir evento de cátedra a entidad: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Actualiza un evento existente con datos de cátedra (merge)
     */
    public void updateEntity(Evento eventoExistente, CatedraEventoCompletoDTO catedraEvento) {
        if (eventoExistente == null || catedraEvento == null) {
            return;
        }

        try {
            // Actualizar tipo de evento si cambió
            if (catedraEvento.getEventoTipo() != null) {
                String nombreTipo = catedraEvento.getEventoTipo().getNombre();
                TipoEvento tipoEvento = tipoEventoRepository.findByNombre(nombreTipo)
                        .orElseGet(() -> {
                            TipoEvento nuevoTipo = TipoEvento.builder()
                                    .nombre(nombreTipo)
                                    .descripcion(catedraEvento.getEventoTipo().getDescripcion())
                                    .build();
                            return tipoEventoRepository.save(nuevoTipo);
                        });
                eventoExistente.setTipoEvento(tipoEvento);
            }

            // Actualizar campos básicos
            eventoExistente.setTitulo(catedraEvento.getTitulo());
            eventoExistente.setResumen(catedraEvento.getResumen());
            eventoExistente.setDescripcion(catedraEvento.getDescripcion());
            eventoExistente.setDireccion(catedraEvento.getDireccion());
            eventoExistente.setImagen(catedraEvento.getImagen());
            
            // Usar valores por defecto si no están especificados (10 filas x 16 columnas)
            eventoExistente.setFilaAsientos(
                catedraEvento.getFilaAsientos() != null ? catedraEvento.getFilaAsientos() : 10
            );
            eventoExistente.setColumnaAsientos(
                catedraEvento.getColumnAsientos() != null ? catedraEvento.getColumnAsientos() : 16
            );
            eventoExistente.setPrecioEntrada(catedraEvento.getPrecioEntrada());

            // Actualizar fecha
            if (catedraEvento.getFecha() != null) {
                try {
                    ZonedDateTime fecha = ZonedDateTime.parse(catedraEvento.getFecha(), DateTimeFormatter.ISO_DATE_TIME);
                    eventoExistente.setFecha(fecha.toLocalDateTime());
                } catch (Exception e) {
                    log.warn("Error al parsear fecha del evento {}: {}", catedraEvento.getId(), e.getMessage());
                }
            }

            // Actualizar integrantes (eliminar existentes y agregar nuevos)
            eventoExistente.clearIntegrantes();
            if (catedraEvento.getIntegrantes() != null) {
                for (CatedraIntegranteDTO catedraIntegrante : catedraEvento.getIntegrantes()) {
                    Integrante integrante = Integrante.builder()
                            .nombre(catedraIntegrante.getNombre())
                            .apellido(catedraIntegrante.getApellido())
                            .identificacion(catedraIntegrante.getIdentificacion())
                            .build();
                    eventoExistente.addIntegrante(integrante);
                }
            }

        } catch (Exception e) {
            log.error("Error al actualizar evento con datos de cátedra: {}", e.getMessage(), e);
        }
    }
}

