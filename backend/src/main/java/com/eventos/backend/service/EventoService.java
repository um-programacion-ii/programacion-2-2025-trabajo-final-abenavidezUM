package com.eventos.backend.service;

import com.eventos.backend.domain.Evento;
import com.eventos.backend.domain.TipoEvento;
import com.eventos.backend.dto.EventoDetalleDTO;
import com.eventos.backend.dto.EventoResumenDTO;
import com.eventos.backend.exception.ResourceNotFoundException;
import com.eventos.backend.mapper.EventoMapper;
import com.eventos.backend.repository.EventoRepository;
import com.eventos.backend.repository.TipoEventoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoService {

    private final EventoRepository eventoRepository;
    private final TipoEventoRepository tipoEventoRepository;
    private final EventoMapper eventoMapper;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    // Prefijos para cache en Redis
    private static final String CACHE_PREFIX_EVENTO = "evento:";
    private static final String CACHE_PREFIX_EVENTOS_LIST = "eventos:list:";
    private static final String CACHE_PREFIX_EVENTOS_SEARCH = "eventos:search:";
    
    // TTL para cache de eventos (10 minutos)
    private static final long CACHE_TTL_MINUTES = 10;

    // ==================== CONSULTAS BÁSICAS ====================

    /**
     * Obtener todos los eventos activos con paginación
     */
    @Transactional(readOnly = true)
    public Page<EventoResumenDTO> findAllActive(Pageable pageable) {
        log.info("Obteniendo eventos activos - página: {}, tamaño: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        String cacheKey = CACHE_PREFIX_EVENTOS_LIST + "active:" + pageable.getPageNumber() + ":" + pageable.getPageSize();
        
        // Intentar obtener de cache
        Page<EventoResumenDTO> cachedPage = getPageFromCache(cacheKey);
        if (cachedPage != null) {
            log.debug("Eventos obtenidos desde cache: {}", cacheKey);
            return cachedPage;
        }
        
        // Obtener de base de datos
        List<Evento> eventos = eventoRepository.findAllActive();
        List<EventoResumenDTO> eventosDTO = eventoMapper.toResumenDTOList(eventos);
        
        // Aplicar paginación manual (ya que el repo devuelve List)
        Page<EventoResumenDTO> page = paginateList(eventosDTO, pageable);
        
        // Guardar en cache
        savePageToCache(cacheKey, page);
        
        log.info("Encontrados {} eventos activos", eventos.size());
        return page;
    }

    /**
     * Obtener detalle completo de un evento por ID
     */
    @Transactional(readOnly = true)
    public EventoDetalleDTO findById(Long id) {
        log.info("Buscando evento por ID: {}", id);
        
        String cacheKey = CACHE_PREFIX_EVENTO + id;
        
        // Intentar obtener de cache
        EventoDetalleDTO cached = getEventoFromCache(cacheKey);
        if (cached != null) {
            log.debug("Evento obtenido desde cache: {}", id);
            return cached;
        }
        
        // Obtener de base de datos
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", id));
        
        if (!evento.getActivo()) {
            throw new ResourceNotFoundException("Evento", "id", id);
        }
        
        EventoDetalleDTO eventoDTO = eventoMapper.toDetalleDTO(evento);
        
        // Guardar en cache
        saveEventoToCache(cacheKey, eventoDTO);
        
        return eventoDTO;
    }

    /**
     * Obtener detalle de un evento por ID externo (ID de cátedra)
     */
    @Transactional(readOnly = true)
    public EventoDetalleDTO findByIdExterno(Long idExterno) {
        log.info("Buscando evento por ID externo: {}", idExterno);
        
        String cacheKey = CACHE_PREFIX_EVENTO + "ext:" + idExterno;
        
        // Intentar obtener de cache
        EventoDetalleDTO cached = getEventoFromCache(cacheKey);
        if (cached != null) {
            log.debug("Evento obtenido desde cache por ID externo: {}", idExterno);
            return cached;
        }
        
        Evento evento = eventoRepository.findByIdExterno(idExterno)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "idExterno", idExterno));
        
        if (!evento.getActivo()) {
            throw new ResourceNotFoundException("Evento", "idExterno", idExterno);
        }
        
        EventoDetalleDTO eventoDTO = eventoMapper.toDetalleDTO(evento);
        
        // Guardar en cache
        saveEventoToCache(cacheKey, eventoDTO);
        
        return eventoDTO;
    }

    // ==================== BÚSQUEDA Y FILTRADO ====================

    /**
     * Buscar eventos por título (búsqueda parcial)
     */
    @Transactional(readOnly = true)
    public Page<EventoResumenDTO> searchByTitulo(String titulo, Pageable pageable) {
        log.info("Buscando eventos por título: '{}'", titulo);
        
        String cacheKey = CACHE_PREFIX_EVENTOS_SEARCH + "titulo:" + titulo.toLowerCase() + 
                ":" + pageable.getPageNumber() + ":" + pageable.getPageSize();
        
        Page<EventoResumenDTO> cachedPage = getPageFromCache(cacheKey);
        if (cachedPage != null) {
            log.debug("Búsqueda obtenida desde cache");
            return cachedPage;
        }
        
        List<Evento> eventos = eventoRepository.findByTituloContaining(titulo);
        List<EventoResumenDTO> eventosDTO = eventoMapper.toResumenDTOList(eventos);
        Page<EventoResumenDTO> page = paginateList(eventosDTO, pageable);
        
        savePageToCache(cacheKey, page);
        
        log.info("Encontrados {} eventos con título que contiene '{}'", eventos.size(), titulo);
        return page;
    }

    /**
     * Obtener eventos futuros
     */
    @Transactional(readOnly = true)
    public Page<EventoResumenDTO> findFutureEvents(Pageable pageable) {
        log.info("Obteniendo eventos futuros");
        
        String cacheKey = CACHE_PREFIX_EVENTOS_LIST + "future:" + pageable.getPageNumber() + ":" + pageable.getPageSize();
        
        Page<EventoResumenDTO> cachedPage = getPageFromCache(cacheKey);
        if (cachedPage != null) {
            return cachedPage;
        }
        
        List<Evento> eventos = eventoRepository.findFutureEvents(LocalDateTime.now());
        List<EventoResumenDTO> eventosDTO = eventoMapper.toResumenDTOList(eventos);
        Page<EventoResumenDTO> page = paginateList(eventosDTO, pageable);
        
        savePageToCache(cacheKey, page);
        
        log.info("Encontrados {} eventos futuros", eventos.size());
        return page;
    }

    /**
     * Obtener eventos pasados
     */
    @Transactional(readOnly = true)
    public Page<EventoResumenDTO> findPastEvents(Pageable pageable) {
        log.info("Obteniendo eventos pasados");
        
        String cacheKey = CACHE_PREFIX_EVENTOS_LIST + "past:" + pageable.getPageNumber() + ":" + pageable.getPageSize();
        
        Page<EventoResumenDTO> cachedPage = getPageFromCache(cacheKey);
        if (cachedPage != null) {
            return cachedPage;
        }
        
        List<Evento> eventos = eventoRepository.findPastEvents(LocalDateTime.now());
        List<EventoResumenDTO> eventosDTO = eventoMapper.toResumenDTOList(eventos);
        Page<EventoResumenDTO> page = paginateList(eventosDTO, pageable);
        
        savePageToCache(cacheKey, page);
        
        log.info("Encontrados {} eventos pasados", eventos.size());
        return page;
    }

    /**
     * Obtener eventos en un rango de fechas
     */
    @Transactional(readOnly = true)
    public Page<EventoResumenDTO> findByDateRange(LocalDateTime inicio, LocalDateTime fin, Pageable pageable) {
        log.info("Buscando eventos entre {} y {}", inicio, fin);
        
        List<Evento> eventos = eventoRepository.findEventsBetweenDates(inicio, fin);
        List<EventoResumenDTO> eventosDTO = eventoMapper.toResumenDTOList(eventos);
        
        log.info("Encontrados {} eventos en el rango de fechas", eventos.size());
        return paginateList(eventosDTO, pageable);
    }

    /**
     * Obtener eventos por tipo
     */
    @Transactional(readOnly = true)
    public Page<EventoResumenDTO> findByTipoEvento(Long tipoEventoId, Pageable pageable) {
        log.info("Buscando eventos por tipo: {}", tipoEventoId);
        
        TipoEvento tipoEvento = tipoEventoRepository.findById(tipoEventoId)
                .orElseThrow(() -> new ResourceNotFoundException("TipoEvento", "id", tipoEventoId));
        
        String cacheKey = CACHE_PREFIX_EVENTOS_LIST + "tipo:" + tipoEventoId + 
                ":" + pageable.getPageNumber() + ":" + pageable.getPageSize();
        
        Page<EventoResumenDTO> cachedPage = getPageFromCache(cacheKey);
        if (cachedPage != null) {
            return cachedPage;
        }
        
        List<Evento> eventos = eventoRepository.findByTipoEventoAndActivoTrue(tipoEvento);
        List<EventoResumenDTO> eventosDTO = eventoMapper.toResumenDTOList(eventos);
        Page<EventoResumenDTO> page = paginateList(eventosDTO, pageable);
        
        savePageToCache(cacheKey, page);
        
        log.info("Encontrados {} eventos del tipo {}", eventos.size(), tipoEvento.getNombre());
        return page;
    }

    /**
     * Búsqueda avanzada con múltiples filtros
     */
    @Transactional(readOnly = true)
    public Page<EventoResumenDTO> searchAdvanced(
            String titulo,
            Long tipoEventoId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            Pageable pageable) {
        
        log.info("Búsqueda avanzada - titulo: '{}', tipo: {}, desde: {}, hasta: {}", 
                titulo, tipoEventoId, fechaDesde, fechaHasta);
        
        // Obtener todos los eventos activos y filtrar en memoria
        List<Evento> eventos = eventoRepository.findAllActive();
        
        // Aplicar filtros
        if (titulo != null && !titulo.trim().isEmpty()) {
            String tituloLower = titulo.toLowerCase();
            eventos = eventos.stream()
                    .filter(e -> e.getTitulo().toLowerCase().contains(tituloLower))
                    .toList();
        }
        
        if (tipoEventoId != null) {
            eventos = eventos.stream()
                    .filter(e -> e.getTipoEvento() != null && e.getTipoEvento().getId().equals(tipoEventoId))
                    .toList();
        }
        
        if (fechaDesde != null) {
            eventos = eventos.stream()
                    .filter(e -> e.getFecha() != null && !e.getFecha().isBefore(fechaDesde))
                    .toList();
        }
        
        if (fechaHasta != null) {
            eventos = eventos.stream()
                    .filter(e -> e.getFecha() != null && !e.getFecha().isAfter(fechaHasta))
                    .toList();
        }
        
        List<EventoResumenDTO> eventosDTO = eventoMapper.toResumenDTOList(eventos);
        
        log.info("Búsqueda avanzada encontró {} resultados", eventos.size());
        return paginateList(eventosDTO, pageable);
    }

    // ==================== ESTADÍSTICAS ====================

    /**
     * Contar eventos activos
     */
    @Transactional(readOnly = true)
    public Long countActiveEvents() {
        return eventoRepository.countActiveEvents();
    }

    // ==================== INVALIDACIÓN DE CACHE ====================

    /**
     * Invalidar cache de un evento específico
     */
    public void invalidateEventoCache(Long eventoId) {
        log.info("Invalidando cache del evento: {}", eventoId);
        redisService.delete(CACHE_PREFIX_EVENTO + eventoId);
    }

    /**
     * Invalidar cache de un evento por ID externo
     */
    public void invalidateEventoCacheByIdExterno(Long idExterno) {
        log.info("Invalidando cache del evento por ID externo: {}", idExterno);
        redisService.delete(CACHE_PREFIX_EVENTO + "ext:" + idExterno);
    }

    /**
     * Invalidar toda la cache de listados de eventos
     * Se llama cuando hay actualizaciones desde sincronización o Kafka
     */
    public void invalidateAllEventosCache() {
        log.info("Invalidando toda la cache de eventos");
        // Nota: En producción, usar SCAN para encontrar y eliminar claves por patrón
        // Por ahora, simplemente dejamos que expiren naturalmente (TTL de 10 min)
    }

    // ==================== MÉTODOS AUXILIARES DE CACHE ====================

    private Page<EventoResumenDTO> getPageFromCache(String cacheKey) {
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                // Deserializar el objeto guardado
                return objectMapper.convertValue(cached, new TypeReference<PageImpl<EventoResumenDTO>>() {});
            }
        } catch (Exception e) {
            log.warn("Error al obtener página de cache: {}", e.getMessage());
        }
        return null;
    }

    private void savePageToCache(String cacheKey, Page<EventoResumenDTO> page) {
        try {
            // Crear un objeto serializable para guardar
            CacheablePage<EventoResumenDTO> cacheablePage = new CacheablePage<>(
                    page.getContent(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements()
            );
            redisService.save(cacheKey, cacheablePage, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Error al guardar página en cache: {}", e.getMessage());
        }
    }

    private EventoDetalleDTO getEventoFromCache(String cacheKey) {
        try {
            Object cached = redisService.get(cacheKey);
            if (cached != null) {
                return objectMapper.convertValue(cached, EventoDetalleDTO.class);
            }
        } catch (Exception e) {
            log.warn("Error al obtener evento de cache: {}", e.getMessage());
        }
        return null;
    }

    private void saveEventoToCache(String cacheKey, EventoDetalleDTO evento) {
        try {
            redisService.save(cacheKey, evento, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Error al guardar evento en cache: {}", e.getMessage());
        }
    }

    private <T> Page<T> paginateList(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());
        
        if (start > list.size()) {
            return new PageImpl<>(List.of(), pageable, list.size());
        }
        
        List<T> pageContent = list.subList(start, end);
        return new PageImpl<>(pageContent, pageable, list.size());
    }

    // ==================== CLASE AUXILIAR PARA CACHE ====================

    /**
     * Clase auxiliar para serializar páginas en Redis
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CacheablePage<T> {
        private List<T> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
    }
}

