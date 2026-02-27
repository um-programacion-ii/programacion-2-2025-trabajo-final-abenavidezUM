package com.eventos.backend.application.service;

import com.eventos.backend.domain.model.Evento;
import com.eventos.backend.dto.*;
import com.eventos.backend.dto.catedra.CatedraAsientoDTO;
import com.eventos.backend.dto.catedra.CatedraBloquearAsientosRequestDTO;
import com.eventos.backend.dto.catedra.CatedraBloquearAsientosResponseDTO;
import com.eventos.backend.dto.proxy.ProxyEstadoAsientoResponseDTO;
import com.eventos.backend.dto.proxy.ProxyMapaAsientosResponseDTO;
import com.eventos.backend.domain.exception.BadRequestException;
import com.eventos.backend.domain.exception.ResourceNotFoundException;
import com.eventos.backend.infrastructure.adapter.output.external.service.CatedraApiClient;
import com.eventos.backend.infrastructure.adapter.output.external.service.ProxyClient;
import com.eventos.backend.application.service.SesionCompraServiceImpl;
import com.eventos.backend.infrastructure.adapter.output.persistence.repository.EventoRepository;
import com.eventos.backend.infrastructure.adapter.output.persistence.repository.AsientoVentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar asientos de eventos.
 * Consulta disponibilidad y bloquea asientos a través del servicio de cátedra.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsientoServiceImpl {

    private final EventoRepository eventoRepository;
    private final CatedraApiClient catedraApiClient;
    private final SesionCompraServiceImpl sesionCompraService;
    private final ProxyClient proxyClient;
    private final AsientoVentaRepository asientoVentaRepository;

    private static final int MAX_ASIENTOS = 4;
    private static final int BLOQUEO_TIMEOUT_MINUTOS = 5;

    /**
     * Obtiene el mapa de asientos de un evento
     */
    public MapaAsientosDTO obtenerMapaAsientos(Long eventoId) {
        log.info("Obteniendo mapa de asientos para evento: {}", eventoId);

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));

        if (!evento.getActivo()) {
            throw new BadRequestException("El evento no está disponible");
        }

        int totalFilas = evento.getFilaAsientos();
        int totalColumnas = evento.getColumnaAsientos();
        int totalAsientos = totalFilas * totalColumnas;

        // Obtener sesión actual para marcar asientos seleccionados
        SesionCompraDTO sesionActual = null;
        try {
            sesionActual = sesionCompraService.obtenerSesionActual();
        } catch (Exception e) {
            log.debug("No hay sesión activa");
        }

        // ✅ Obtener mapa completo de asientos en UNA sola llamada al proxy
        Map<String, String> mapaEstadosProxy = new HashMap<>();
        if (evento.getIdExterno() != null) {
            try {
                ProxyMapaAsientosResponseDTO mapaProxy = 
                        proxyClient.obtenerMapaAsientos(evento.getIdExterno());
                
                if (mapaProxy != null && mapaProxy.getAsientos() != null) {
                    // El proxy ya devuelve el mapa en el formato "fila:col" -> estado
                    mapaEstadosProxy = mapaProxy.getAsientos();
                    log.info("Mapa de asientos obtenido desde proxy: {} asientos", mapaEstadosProxy.size());
                }
            } catch (Exception e) {
                log.warn("Proxy no disponible, consultando BD local para asientos vendidos", e);
            }
        }
        
        // ✅ Consultar asientos vendidos en BD local (complementa o reemplaza info del proxy)
        Set<String> asientosVendidosLocal = new HashSet<>();
        try {
            log.info("🔍 Consultando asientos vendidos para evento ID: {}", eventoId);
            List<com.eventos.backend.domain.model.AsientoVenta> asientosVendidos = 
                    asientoVentaRepository.findAsientosVendidosByEvento(eventoId);
            
            log.info("📊 Query devolvió {} asientos vendidos", asientosVendidos.size());
            
            for (com.eventos.backend.domain.model.AsientoVenta av : asientosVendidos) {
                String key = av.getFila() + ":" + av.getColumna();
                asientosVendidosLocal.add(key);
                // Sobrescribir estado del proxy si el asiento está vendido localmente
                mapaEstadosProxy.put(key, "OCUPADO");
                log.debug("🔴 Marcando asiento vendido: fila={}, col={}, resultado={}", 
                         av.getFila(), av.getColumna(), av.getVenta().getResultado());
            }
            
            if (!asientosVendidosLocal.isEmpty()) {
                log.info("✅ Asientos vendidos encontrados en BD local: {}", asientosVendidosLocal.size());
            } else {
                log.info("ℹ️ No se encontraron asientos vendidos para evento {}", eventoId);
            }
        } catch (Exception e) {
            log.error("❌ Error al consultar asientos vendidos localmente", e);
        }
        
        // Generar matriz de asientos
        List<EstadoAsientoDTO> asientos = new ArrayList<>();
        int libres = 0;
        int ocupados = 0;
        int bloqueados = 0;

        for (int fila = 1; fila <= totalFilas; fila++) {
            for (int col = 1; col <= totalColumnas; col++) {
                String estado = EstadoAsientoDTO.LIBRE;
                
                // Obtener estado desde el mapa del proxy (ya cargado)
                String key = fila + ":" + col;
                String estadoProxy = mapaEstadosProxy.get(key);
                if (estadoProxy != null) {
                    estado = mapearEstadoDeProxy(estadoProxy);
                }
                
                // Verificar si está seleccionado en sesión actual (tiene prioridad)
                if (sesionActual != null && sesionActual.getEventoId().equals(eventoId)) {
                    final int f = fila;
                    final int c = col;
                    boolean seleccionado = sesionActual.getAsientosSeleccionados().stream()
                            .anyMatch(a -> a.getFila().equals(f) && a.getColumna().equals(c));
                    if (seleccionado) {
                        estado = EstadoAsientoDTO.SELECCIONADO;
                    }
                }

                if (estado.equals(EstadoAsientoDTO.LIBRE)) libres++;
                else if (estado.equals(EstadoAsientoDTO.OCUPADO)) ocupados++;
                else if (estado.equals(EstadoAsientoDTO.BLOQUEADO)) bloqueados++;

                asientos.add(EstadoAsientoDTO.builder()
                        .fila(fila)
                        .columna(col)
                        .estado(estado)
                        .build());
            }
        }

        return MapaAsientosDTO.builder()
                .eventoId(eventoId)
                .totalFilas(totalFilas)
                .totalColumnas(totalColumnas)
                .asientosTotales(totalAsientos)
                .asientosLibres(libres)
                .asientosOcupados(ocupados)
                .asientosBloqueados(bloqueados)
                .asientos(asientos)
                .build();
    }

    /**
     * Bloquea asientos en el servicio de cátedra
     */
    public BloquearAsientosResponseDTO bloquearAsientos(Long eventoId, List<AsientoSeleccionadoDTO> asientos) {
        log.info("Bloqueando {} asientos para evento: {}", asientos.size(), eventoId);

        // ✅ PASO 1: Validar que existe una sesión activa
        SesionCompraDTO sesion = sesionCompraService.obtenerSesionActual();
        if (sesion == null || sesion.isExpirada()) {
            log.warn("No hay sesión activa o está expirada");
            return BloquearAsientosResponseDTO.builder()
                    .exitoso(false)
                    .mensaje("Sesión de compra no encontrada o expirada")
                    .asientosBloqueados(List.of())
                    .build();
        }

        // ✅ PASO 2: Verificar que la sesión es para este evento
        if (!sesion.getEventoId().equals(eventoId)) {
            log.warn("La sesión es para otro evento. Sesión: {}, Solicitado: {}", 
                     sesion.getEventoId(), eventoId);
            return BloquearAsientosResponseDTO.builder()
                    .exitoso(false)
                    .mensaje("La sesión no corresponde al evento seleccionado")
                    .asientosBloqueados(List.of())
                    .build();
        }

        // Validar cantidad
        if (asientos.size() > MAX_ASIENTOS) {
            throw new BadRequestException("No puede bloquear más de " + MAX_ASIENTOS + " asientos");
        }

        // Validar evento
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", eventoId));

        if (!evento.getActivo()) {
            throw new BadRequestException("El evento no está disponible");
        }

        if (evento.getIdExterno() == null) {
            throw new BadRequestException("El evento no tiene ID externo configurado");
        }

        // Validar rango de asientos
        for (AsientoSeleccionadoDTO asiento : asientos) {
            if (asiento.getFila() < 1 || asiento.getFila() > evento.getFilaAsientos()) {
                throw new BadRequestException("Fila " + asiento.getFila() + " no válida");
            }
            if (asiento.getColumna() < 1 || asiento.getColumna() > evento.getColumnaAsientos()) {
                throw new BadRequestException("Columna " + asiento.getColumna() + " no válida");
            }
        }

        // ✅ PASO 3: Actualizar sesión con los asientos antes de bloquear
        sesionCompraService.actualizarAsientos(asientos);

        // Convertir a formato de cátedra
        List<CatedraAsientoDTO> asientosCatedra = asientos.stream()
                .map(a -> CatedraAsientoDTO.builder()
                        .fila(a.getFila())
                        .columna(a.getColumna())
                        .build())
                .collect(Collectors.toList());

        CatedraBloquearAsientosRequestDTO request = CatedraBloquearAsientosRequestDTO.builder()
                .eventoId(evento.getIdExterno())
                .asientos(asientosCatedra)
                .build();

        try {
            // Llamar a cátedra para bloquear
            CatedraBloquearAsientosResponseDTO response = catedraApiClient.bloquearAsientos(request);

            if (response != null && Boolean.TRUE.equals(response.getResultado())) {
                // Marcar asientos como bloqueados en la sesión
                sesionCompraService.marcarAsientosBloqueados();

                log.info("Asientos bloqueados exitosamente");
                return BloquearAsientosResponseDTO.builder()
                        .exitoso(true)
                        .mensaje("Asientos bloqueados exitosamente")
                        .asientosBloqueados(asientos)
                        .expiracion(LocalDateTime.now().plusMinutes(BLOQUEO_TIMEOUT_MINUTOS))
                        .build();
            } else {
                String desc = response != null ? response.getDescripcion() : "Error desconocido";
                log.warn("No se pudieron bloquear los asientos: {}", desc);
                return BloquearAsientosResponseDTO.builder()
                        .exitoso(false)
                        .mensaje(desc)
                        .asientosBloqueados(List.of())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error al bloquear asientos: {}", e.getMessage(), e);
            return BloquearAsientosResponseDTO.builder()
                    .exitoso(false)
                    .mensaje("Error al comunicarse con el servicio: " + e.getMessage())
                    .asientosBloqueados(List.of())
                    .build();
        }
    }

    /**
     * Libera los asientos bloqueados del usuario actual
     */
    public void liberarAsientos() {
        log.info("Liberando asientos bloqueados");
        
        SesionCompraDTO sesion = sesionCompraService.obtenerSesionActual();
        if (sesion == null || sesion.getAsientosSeleccionados().isEmpty()) {
            log.info("No hay asientos para liberar");
            return;
        }

        // TODO: Llamar a cátedra para liberar los asientos bloqueados
        // Por ahora, solo limpiamos la sesión
        
        sesionCompraService.limpiarSesion();
        log.info("Sesión y asientos liberados");
    }

    /**
     * Mapea estados del proxy (Redis de cátedra) a estados del frontend
     * 
     * Estados del proxy: LIBRE, BLOQUEADO, VENDIDO, OCUPADO
     * Estados del frontend: LIBRE, BLOQUEADO, OCUPADO, SELECCIONADO
     * 
     * @param estadoProxy Estado desde Redis de cátedra
     * @return Estado para el frontend
     */
    private String mapearEstadoDeProxy(String estadoProxy) {
        if (estadoProxy == null) {
            return EstadoAsientoDTO.LIBRE;
        }
        
        // ✅ La cátedra usa estados con primera letra mayúscula: "Libre", "Bloqueado", "Vendido"
        switch (estadoProxy) {
            case "Libre":
                return EstadoAsientoDTO.LIBRE;
            case "Bloqueado":
                return EstadoAsientoDTO.BLOQUEADO;
            case "Vendido":
            case "Ocupado": // Por compatibilidad
                return EstadoAsientoDTO.OCUPADO;
            default:
                // Intentar con mayúsculas por si acaso
                switch (estadoProxy.toUpperCase()) {
                    case "LIBRE":
                        return EstadoAsientoDTO.LIBRE;
                    case "BLOQUEADO":
                        return EstadoAsientoDTO.BLOQUEADO;
                    case "VENDIDO":
                    case "OCUPADO":
                        return EstadoAsientoDTO.OCUPADO;
                    default:
                        log.warn("Estado desconocido desde proxy: {}", estadoProxy);
                        return EstadoAsientoDTO.LIBRE;
                }
        }
    }
}

