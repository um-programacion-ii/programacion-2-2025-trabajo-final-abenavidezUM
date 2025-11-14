package com.eventos.backend.service;

import com.eventos.backend.domain.Evento;
import com.eventos.backend.dto.catedra.CatedraEventoCompletoDTO;
import com.eventos.backend.mapper.CatedraEventoMapper;
import com.eventos.backend.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoSyncService {

    private final CatedraApiClient catedraApiClient;
    private final EventoRepository eventoRepository;
    private final CatedraEventoMapper catedraEventoMapper;

    /**
     * Sincronización completa de todos los eventos desde cátedra
     */
    @Transactional
    public int sincronizarTodos() {
        log.info("Iniciando sincronización completa de eventos desde cátedra");
        
        try {
            // Obtener todos los eventos completos desde cátedra
            List<CatedraEventoCompletoDTO> eventosC

atedra = catedraApiClient.obtenerEventosCompletos();
            
            if (eventosC atedra == null || eventosC atedra.isEmpty()) {
                log.warn("No se obtuvieron eventos desde cátedra");
                return 0;
            }

            int eventosCreados = 0;
            int eventosActualizados = 0;
            LocalDateTime ahora = LocalDateTime.now();

            for (CatedraEventoCompletoDTO catedraEvento : eventosC atedra) {
                try {
                    // Buscar si el evento ya existe localmente por ID externo
                    Optional<Evento> eventoExistente = eventoRepository.findByIdExternoAndActivoTrue(catedraEvento.getId());

                    if (eventoExistente.isPresent()) {
                        // Actualizar evento existente
                        Evento evento = eventoExistente.get();
                        catedraEventoMapper.updateEntity(evento, catedraEvento);
                        evento.setUltimaSincronizacion(ahora);
                        eventoRepository.save(evento);
                        eventosActualizados++;
                        log.debug("Evento actualizado: {} (ID externo: {})", evento.getTitulo(), catedraEvento.getId());
                    } else {
                        // Crear nuevo evento
                        Evento nuevoEvento = catedraEventoMapper.toEntity(catedraEvento);
                        if (nuevoEvento != null) {
                            nuevoEvento.setUltimaSincronizacion(ahora);
                            eventoRepository.save(nuevoEvento);
                            eventosCreados++;
                            log.debug("Evento creado: {} (ID externo: {})", nuevoEvento.getTitulo(), catedraEvento.getId());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error al sincronizar evento con ID externo {}: {}", catedraEvento.getId(), e.getMessage(), e);
                }
            }

            // Marcar como inactivos los eventos locales que no están en cátedra
            marcarEventosInactivos(eventosC atedra, ahora);

            log.info("Sincronización completa finalizada: {} creados, {} actualizados", eventosCreados, eventosActualizados);
            return eventosCreados + eventosActualizados;

        } catch (Exception e) {
            log.error("Error en sincronización completa de eventos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al sincronizar eventos desde cátedra", e);
        }
    }

    /**
     * Sincronización incremental de un evento específico
     */
    @Transactional
    public Evento sincronizarEvento(Long idExterno) {
        log.info("Sincronizando evento con ID externo: {}", idExterno);
        
        try {
            // Obtener el evento desde cátedra
            CatedraEventoCompletoDTO catedraEvento = catedraApiClient.obtenerEventoPorId(idExterno);
            
            if (catedraEvento == null) {
                log.warn("Evento con ID externo {} no encontrado en cátedra", idExterno);
                
                // Marcar como inactivo si existe localmente
                Optional<Evento> eventoLocal = eventoRepository.findByIdExternoAndActivoTrue(idExterno);
                if (eventoLocal.isPresent()) {
                    Evento evento = eventoLocal.get();
                    evento.setActivo(false);
                    evento.setUltimaSincronizacion(LocalDateTime.now());
                    eventoRepository.save(evento);
                    log.info("Evento local marcado como inactivo: {}", evento.getTitulo());
                }
                
                return null;
            }

            LocalDateTime ahora = LocalDateTime.now();
            Optional<Evento> eventoExistente = eventoRepository.findByIdExternoAndActivoTrue(catedraEvento.getId());

            if (eventoExistente.isPresent()) {
                // Actualizar evento existente
                Evento evento = eventoExistente.get();
                catedraEventoMapper.updateEntity(evento, catedraEvento);
                evento.setUltimaSincronizacion(ahora);
                evento.setActivo(true); // Asegurar que está activo
                eventoRepository.save(evento);
                log.info("Evento actualizado: {}", evento.getTitulo());
                return evento;
            } else {
                // Crear nuevo evento
                Evento nuevoEvento = catedraEventoMapper.toEntity(catedraEvento);
                if (nuevoEvento != null) {
                    nuevoEvento.setUltimaSincronizacion(ahora);
                    nuevoEvento.setActivo(true);
                    eventoRepository.save(nuevoEvento);
                    log.info("Evento creado: {}", nuevoEvento.getTitulo());
                    return nuevoEvento;
                }
            }

            return null;

        } catch (Exception e) {
            log.error("Error al sincronizar evento con ID externo {}: {}", idExterno, e.getMessage(), e);
            throw new RuntimeException("Error al sincronizar evento desde cátedra", e);
        }
    }

    /**
     * Marca como inactivos los eventos locales que no están en la lista de cátedra
     */
    private void marcarEventosInactivos(List<CatedraEventoCompletoDTO> eventosCatedra, LocalDateTime ahora) {
        try {
            // Obtener todos los IDs externos de cátedra
            List<Long> idsExternosCatedra = eventosCatedra.stream()
                    .map(CatedraEventoCompletoDTO::getId)
                    .toList();

            // Buscar eventos locales activos que no están en cátedra
            List<Evento> eventosLocales = eventoRepository.findByActivoTrue();
            int eventosDesactivados = 0;

            for (Evento eventoLocal : eventosLocales) {
                if (eventoLocal.getIdExterno() != null && !idsExternosCatedra.contains(eventoLocal.getIdExterno())) {
                    eventoLocal.setActivo(false);
                    eventoLocal.setUltimaSincronizacion(ahora);
                    eventoRepository.save(eventoLocal);
                    eventosDesactivados++;
                    log.info("Evento marcado como inactivo: {} (ID externo: {})", eventoLocal.getTitulo(), eventoLocal.getIdExterno());
                }
            }

            if (eventosDesactivados > 0) {
                log.info("Se marcaron {} eventos como inactivos", eventosDesactivados);
            }

        } catch (Exception e) {
            log.error("Error al marcar eventos como inactivos: {}", e.getMessage(), e);
        }
    }

    /**
     * Sincronización automática programada (cada 30 minutos)
     */
    @Scheduled(fixedDelayString = "${sync.eventos.interval:1800000}") // 30 minutos por defecto
    @Transactional
    public void sincronizacionAutomatica() {
        log.info("Ejecutando sincronización automática de eventos");
        try {
            sincronizarTodos();
        } catch (Exception e) {
            log.error("Error en sincronización automática: {}", e.getMessage(), e);
        }
    }

    /**
     * Obtiene la última fecha de sincronización
     */
    public LocalDateTime obtenerUltimaSincronizacion() {
        return eventoRepository.findFirstByOrderByUltimaSincronizacionDesc()
                .map(Evento::getUltimaSincronizacion)
                .orElse(null);
    }
}

