package com.eventos.backend.infrastructure.adapter.output.external.service;

import com.eventos.backend.domain.model.Evento;
import com.eventos.backend.dto.*;
import com.eventos.backend.dto.catedra.CatedraAsientoDTO;
import com.eventos.backend.dto.catedra.CatedraBloquearAsientosRequestDTO;
import com.eventos.backend.dto.catedra.CatedraBloquearAsientosResponseDTO;
import com.eventos.backend.dto.proxy.ProxyEstadoAsientoResponseDTO;
import com.eventos.backend.domain.exception.BadRequestException;
import com.eventos.backend.domain.exception.ResourceNotFoundException;
import com.eventos.backend.infrastructure.adapter.output.persistence.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar asientos de eventos.
 * Consulta disponibilidad y bloquea asientos a través del servicio de cátedra.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsientoService {

    private final EventoRepository eventoRepository;
    private final CatedraApiClient catedraApiClient;
    private final SesionCompraService sesionCompraService;
    private final ProxyClient proxyClient;

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

        // Generar matriz de asientos
        List<EstadoAsientoDTO> asientos = new ArrayList<>();
        int libres = 0;
        int ocupados = 0;
        int bloqueados = 0;

        for (int fila = 1; fila <= totalFilas; fila++) {
            for (int col = 1; col <= totalColumnas; col++) {
                String estado = EstadoAsientoDTO.LIBRE;
                
                // Consultar estado real desde Redis de cátedra vía Proxy
                if (evento.getIdExterno() != null) {
                    try {
                        ProxyEstadoAsientoResponseDTO estadoProxy = 
                                proxyClient.obtenerEstadoAsiento(evento.getIdExterno(), fila, col);
                        
                        if (estadoProxy != null && estadoProxy.getEstado() != null) {
                            // Mapear estados del proxy a estados del frontend
                            estado = mapearEstadoDeProxy(estadoProxy.getEstado());
                            log.debug("Estado asiento {}:{} desde proxy: {} -> {}", 
                                    fila, col, estadoProxy.getEstado(), estado);
                        }
                    } catch (Exception e) {
                        log.warn("Error al consultar estado de asiento {}:{} en proxy, usando LIBRE por defecto", 
                                fila, col);
                    }
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
                // NO llamar a actualizarAsientos() porque borraría las personas ya cargadas
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

